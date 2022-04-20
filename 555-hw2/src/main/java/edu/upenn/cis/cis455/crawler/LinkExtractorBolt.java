package edu.upenn.cis.cis455.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.model.PutObjectRequest;

import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.ChecksumItem;
import edu.upenn.cis.cis455.storage.DocidToUrlItem;
import edu.upenn.cis.cis455.storage.DomainItem;
import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.UrlToDocIdItem;
import edu.upenn.cis.cis455.storage.visitedURLItem;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

public class LinkExtractorBolt implements IRichBolt{
	static Logger logger = LogManager.getLogger(LinkExtractorBolt.class);

	Fields schema = new Fields("extractedUrl"); 
	
    String executorId = UUID.randomUUID().toString();
    
    DynamoDBMapper mapper;

    private OutputCollector collector;

    public LinkExtractorBolt() {}
    
	@Override
	public String getExecutorId() {
		return executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);		
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(Tuple input) {
		Crawler.getItem().setWorking(true);
		String result = input.getStringByField("document");
		String url = input.getStringByField("url");
		String type = input.getStringByField("type");
		
		if(result==null) { return; }
		String hostname = new URLInfo(url).getHostName();
		//get the hash
		byte[] digest; 
		try {
			digest = getContentHash(result);
			ChecksumItem item = mapper.load(ChecksumItem.class, new String(digest));
			if(item==null) {
	        	checkDocLimits();
	        	updateDatabase(url, result, digest, hostname, type);
	        	if(type.equals("text/html")) { 
	        		addExtractUrl(result, url);
	        	}
	        	checkDocLimits();
	        }
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("DataBase is closed");
		}
		Crawler.getItem().setWorking(false);
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		mapper = new DynamoDBMapper(((Storage)Crawler.getItem().db).dynamoDB);
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);		
	}

	@Override
	public Fields getSchema() {
		return schema;
	}
	
	public ArrayList<String> parseUrl(String content) {
		Document doc = Jsoup.parse(content);
		Elements links = doc.select("a[href]");
        Element link;
        ArrayList<String> urls = new ArrayList<>();
        for(int j=0;j<links.size();j++){
            link=links.get(j);
            String item = link.attr("href").toString();
            if(item.length()>0) {
            	urls.add(item); 
            }
        }
        return urls;
	}
	private byte[] getContentHash(String result) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
	    md.update(result.getBytes());
	    byte[] digest = md.digest();
	    return digest;
	}
	
	private void checkDocLimits() {
		if(Crawler.getItem().getNumOfFile()>=Crawler.getItem().count) {
			Crawler.getItem().notifyThreadExited();
    	}
	}
	
	private void updateDatabase(String url, String result, byte[] digest, String domain, String type) throws IOException {
		
    	//if it not contains the hash,  add it into contentSeen table
		ChecksumItem checksumItem = new ChecksumItem();
		checksumItem.setHash(new String(digest));
//		----mapper.save(checksumItem);
		
//		Crawler.getItem().contentSeen.addHash(digest);
        //add document to db
		
//		Crawler.getItem().docTable.addDocByUrl(url, result, type);

		UrlToDocIdItem urlToDocidItem = new UrlToDocIdItem();
		urlToDocidItem.setUrl(url);
		String docid =  urlToDocidItem.getDocid();
//		----mapper.save(urlToDocidItem);
		
		DocidToUrlItem docidToUrlItem = new DocidToUrlItem();
		docidToUrlItem.setUrl(url);
		docidToUrlItem.setDocid(docid);
//		----mapper.save(docidToUrlItem);
		
		((Storage)Crawler.getItem().db).s3client.putObject(
				credentialSet.documentBucket,
				docid,
				result);
		
		visitedURLItem urlItem = new visitedURLItem();
		urlItem.setUrl(url);
//		----mapper.save(urlItem);
		
//		Crawler.getItem().urlToTimeTable.addUrlToStore(url);\
		DomainItem item = mapper.load(DomainItem.class, domain);
    	if(item!=null) {
    		item.setLastAccessTime(System.currentTimeMillis());
//    		---mapper.save(item);
//    		Crawler.getItem().domainTable.updateEntry(domain);
    	}else {
    		item = new DomainItem();
    		item.setDelay(RobotsHandler.getDelay());
    		item.setDomainName(domain);
    		item.setLastAccessTime(System.currentTimeMillis());
//    		---mapper.save(item);
//    		Crawler.getItem().domainTable.addDomain(domain, RobotsHandler.getDelay());
    	}
    	mapper.batchSave(checksumItem, urlToDocidItem, docidToUrlItem, urlItem, item);
    	logger.info(url+": downloading");
		Crawler.getItem().incCount();
	}
	private void addExtractUrl(String result, String url) {
		ArrayList<String> urls = parseUrl(result);
    	//store urls to queue
    	for(String one: urls) {
    		String processedUrl = processURL(one, url);
    		if(processedUrl.length()==0) { continue; }
    		if(!one.contains(".")&&!processedUrl.endsWith("/")) { processedUrl+="/"; }
    		collector.emit(new Values<Object>(processedUrl));
    	}
	}
	private String processURL(String one, String url) {
		String processedUrl = "";
		if(one.startsWith("http")) {
			processedUrl = one.endsWith("/")?one:one+"/";
		}else if(one.startsWith("//")) {
			if(url.startsWith("https")) {
				processedUrl = "https:"+one;
			}else {
				processedUrl = "http:"+one;
			}
		}else if(one.startsWith("/")){
			processedUrl = url.endsWith("/")?url.substring(0, url.length()-1)+one:url+one;
		}else {
			if(url.endsWith("/")) {
				processedUrl=url+one;
			}else {
				String[] parts = url.split("/");
				parts[parts.length-1]=one;
				processedUrl = String.join("/", parts);
			}
		}
		return processedUrl;
	}

}
