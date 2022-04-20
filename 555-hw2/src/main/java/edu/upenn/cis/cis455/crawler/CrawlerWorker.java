//package edu.upenn.cis.cis455.crawler;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//import javax.net.ssl.HttpsURLConnection;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import edu.upenn.cis.cis455.crawler.utils.URLInfo;
//import spark.utils.IOUtils;
//
//
//public class CrawlerWorker implements Runnable{
//	
//	static final Logger logger = LogManager.getLogger(CrawlerWorker.class);	
//	
//	Crawler cralwer = null;
//	int index = -1;
//	int NUM_WORKERS = 0;
//	ConcurrentHashMap<Integer, ArrayList<String>> frontier = null;
//	ContentSeenTable contentSeen = null;
//	DocumentTable docTable = null;
//	UrlToTimeTable timeTable = null;
//	domainAccessTable domainTable = null;
//	int maxSize = 0;
//	long NumOfFile = 0;
//	int count = 0;
//	
//	public CrawlerWorker(Crawler cralwer, int index, int NUM_WORKERS, ConcurrentHashMap<Integer, ArrayList<String>> frontier, ContentSeenTable contentSeen, DocumentTable docTable, UrlToTimeTable timeTable, domainAccessTable domainTable,int size, long numOfFile, int count){
//		this.cralwer = cralwer;
//		this.index=index;
//		this.NUM_WORKERS = NUM_WORKERS;
//		this.frontier = frontier;
//		this.contentSeen = contentSeen;
//		this.docTable = docTable;
//		this.timeTable = timeTable;
//		this.domainTable = domainTable;
//		this.maxSize = size;
//		this.NumOfFile = numOfFile;
//		this.count = count;
//	}
//
//
//	@Override
//	public void run() {
//		while(!cralwer.isDone()) {
//			cralwer.setWorking(false);
//			try {
//				String elementReadFromQueue = readFromQueue();
//				//process url
//				cralwer.setWorking(true);
//				processURL(elementReadFromQueue);
//				cralwer.setWorking(false);
//				Thread.sleep(100);
//			} catch (InterruptedException ex) {
//				logger.error("Interrupt Exception in Consumer thread");
//			}
//		}
//	}
//
//	private String readFromQueue() throws InterruptedException {
//		while (true) {
//			synchronized (frontier) {
//				if (emptySet(frontier)) {
//					//If the queue is empty, we push the current thread to waiting state. Way to avoid polling.
//					frontier.wait();
//				} else if(frontier.get(index)!=null && !frontier.get(index).isEmpty()){
//					String currUrl = frontier.get(index).remove(0);
//					frontier.notifyAll();
//					return currUrl; 
//				} else {
//					frontier.wait();
//				}
//			}
//		}
//	}
//	private void processURL(String url) {
//		//check the url is stored or not
//		HashMap<String, String> info = new HashMap<>();
//		URLInfo urlInfo = new URLInfo(url);
//		if(!sendHeadRequestAndGetResponse(url, info)) {
//			return;
//		};
//		String type = info.get("type");
//		String userAgent = info.get("User-Agent");
//		String hostname = urlInfo.getHostName();
//		int size = info.get("size")!=null? Integer.parseInt(info.get("size")):0;
//		long modifiedTime = info.get("modifiedTime")!=null? Long.parseLong(info.get("modifiedTime")): 0;
//		
//		//if the path is refused by robots
//		RuleItem theRule = RobotsHandler.getTheRule(url, userAgent, hostname);
//		if(theRule!=null && !RobotsHandler.handleRobotFile(theRule)) {
//			logger.info("url is refused by robots: "+url);
//			return ;
//		}
//		//check the delay if it is stored, check the delay
//		//check conditions checkSize(url, size) && checkType(url, type)
//		if(!checkDelayTime(url, userAgent, hostname)||!checkSize(url, size)||!checkType(url, type)) {
//			return ;
//		}; 
//		
//		try {
//			//get document
//			String result = null;
//			if(!checkModifiedTime(url, modifiedTime)) {
//				result = this.docTable.getDocument(url).getContent();
//			}
//			result = getContent(url);	
//			if(result==null) { return; }
//			//get the hash
//			byte[] digest = getContentHash(result);
//			//check the MD5 hash of the document is in the contentSeen table or no
//	        if(!contentSeen.hashContains(digest)) {
//	        	checkDocLimits();
//	        	if(checkModifiedTime(url, modifiedTime)) {
//	        		updateDatabase(url, result, digest, hostname, type);
//	        	}
//	        	//get all url in this doc
//	        	if(type.equals("text/html")) { addExtractUrl(result, url);}
//	        	checkDocLimits();
//			}else {
//	        	logger.info("skip this stored document "+url);
//			}
//		} catch (NoSuchAlgorithmException e) {
//			System.out.println("get not get hash of the content");
//		} catch(Exception e) {
//			System.out.println("The cralwer is stopped.");
//		}
//	}
//	
//	private void checkDocLimits() {
//		if(cralwer.getNumOfFile()>=count) {
//    		cralwer.notifyThreadExited();
//    	}
//	}
//	
//	private byte[] getContentHash(String result) throws NoSuchAlgorithmException {
//		MessageDigest md = MessageDigest.getInstance("MD5");
//	    md.update(result.getBytes());
//	    byte[] digest = md.digest();
//	    return digest;
//	}
//	
//	public String getContent(String url) {
//		try {
//			URL urlObj = new URL(url);
//			HttpURLConnection httpCon = null; 
//			HttpsURLConnection httpsCon = null;
//			String result = null;
//			if(url.startsWith("https")) {
//				httpsCon = (HttpsURLConnection) urlObj.openConnection();
//				httpsCon.setRequestMethod("GET");
//				httpsCon.setRequestProperty("User-Agent", "cis455crawler");
//				httpsCon.connect();
//			}else {
//				httpCon = (HttpURLConnection) urlObj.openConnection();
//				httpCon.setRequestMethod("GET");
//				httpCon.setRequestProperty("User-Agent", "cis455crawler");
//				httpCon.connect();
//			}
//			
//			int responseCode = httpCon!=null ? httpCon.getResponseCode(): httpsCon.getResponseCode();
//			if (responseCode != HttpURLConnection.HTTP_OK) {
//			    System.out.println("Server returned response code " + responseCode + ". Download failed.");
//			}else {
//				InputStream inputStream = httpCon != null ? httpCon.getInputStream() : httpsCon.getInputStream();
//				result = convertInputStreamToString(inputStream);
//			}
//			if(httpCon != null) {
//				httpCon.disconnect();
//			}else {
//				httpsCon.disconnect();
//			}
//			return result;
//		} catch (MalformedURLException e) {
//			System.out.println("not valid url: "+url);
//		} catch (IOException e) {
//			System.out.println("The connection goes wrong.");
//		}
//		return null;
//	}
//	
//	private String convertInputStreamToString(InputStream inputStream) {
//		String text = new BufferedReader(
//			      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
//			        .lines()
//			        .collect(Collectors.joining("\r\n"));
//		return text;
//	}
//	
//	public ArrayList<String> parseUrl(String content) {
//		Document doc = Jsoup.parse(content);
//		Elements links = doc.select("a[href]");
//        Element link;
//        ArrayList<String> urls = new ArrayList<>();
//        for(int j=0;j<links.size();j++){
//            link=links.get(j);
//            String item = link.attr("href").toString();
//            if(item.length()>0) {
//            	urls.add(item); 
//            }
//        }
//        return urls;
//	}
//	
//	public boolean sendHeadRequestAndGetResponse(String url, HashMap<String, String> info) {
//		try {
//			URL urlObj = new URL(url);
//			HttpURLConnection httpCon = null;
//			HttpsURLConnection httpsCon = null;
//			if(url.startsWith("https")) {
//				httpsCon = (HttpsURLConnection) urlObj.openConnection();
//				httpsCon.setRequestMethod("HEAD");
//				httpsCon.setRequestProperty("User-Agent", "cis455crawler");
//				httpsCon.connect();
//				if(httpsCon.getResponseCode()!=HttpURLConnection.HTTP_OK) {
//					System.out.println(url+"not 200 ok, the status code is "+httpsCon.getResponseCode());
//					return false;
//				}
//				info.put("type", httpsCon.getContentType().split(";")[0]);
//				info.put("size", String.valueOf(httpsCon.getContentLength()));
//				info.put("modifiedTime", String.valueOf(httpsCon.getIfModifiedSince()));
//				info.put("User-Agent", httpsCon.getRequestProperty("User-Agent"));
//				httpsCon.disconnect();
//			}else {
//				httpCon = (HttpURLConnection) urlObj.openConnection();
//				httpCon.setRequestMethod("HEAD");
//				httpCon.setRequestProperty("User-Agent", "cis455crawler");
//				httpCon.connect();
//				if(httpCon.getResponseCode()!=HttpURLConnection.HTTP_OK) {
//					System.out.println("not 200 ok, the status code is "+httpCon.getResponseCode());
//					return false;
//				}
//				info.put("type", httpCon.getContentType().split(";")[0]);
//				info.put("size", String.valueOf(httpCon.getContentLength()));
//				info.put("modifiedTime", String.valueOf(httpCon.getIfModifiedSince()));
//				info.put("User-Agent", httpCon.getRequestProperty("User-Agent"));
//				httpCon.disconnect();
//			}
//			return true;
//			
//		} catch (MalformedURLException e) {
//			System.out.println("invalid url: "+url);
//			return false;
//		} catch (IOException e) {
//			return false;
//		} catch(Exception e) {
//			System.out.println("exception %%%%%%%%%%%%%%%%%%%%%invalid url: "+url);
//			return false;
//		}
//	}
//	
//	private boolean checkDelayTime(String url, String userAgent, String hostname) {
//		if(domainTable.findDomain(hostname)!=null && RobotsHandler.getDelayTime(domainTable, url, userAgent, hostname)>0) {
//			int delay = RobotsHandler.getDelayTime(domainTable,url, userAgent, hostname);
//			long diff = System.currentTimeMillis()-RobotsHandler.getLastAccessTime(domainTable, hostname);
//			if(diff<delay*1000) {
//				frontier.get(index).add(url);
//				return false;
//			}
//		}
//		return true;
//	}
//	private void updateDatabase(String url, String result, byte[] digest, String domain, String type) {
//		logger.info(url+": downloading");
//		cralwer.incCount();
//    	//if it not contains the hash,  add it into contentSeen table
//    	contentSeen.addHash(digest);
//        //add document to db
//    	docTable.addDocByUrl(url, result, type);
//    	timeTable.addUrlToStore(url);
//    	if(domainTable.findDomain(domain)!=null) {
//    		domainTable.updateEntry(domain);
//    	}else {
//    		domainTable.addDomain(domain, RobotsHandler.getDelay());
//    	}
//	}
//	
//	private void addExtractUrl(String result, String url) {
//		ArrayList<String> urls = parseUrl(result);
//    	//store urls to queue
//    	for(String one: urls) {
//    		String processedUrl = processURL(one, url);
//    		if(processedUrl.length()==0) { continue; }
//    		int pos = getHashPos(processedUrl, NUM_WORKERS);
//			if(!frontier.containsKey(pos)) {
//				frontier.put(pos, new ArrayList<String>());
//			}
//			frontier.get(pos).add(processedUrl);
//    	}
//	}
//	
//	private String processURL(String one, String url) {
//		String processedUrl = "";
//		if(one.startsWith("http")) {
//			processedUrl = one.endsWith("/")?one:one+"/";
//		}else if(one.startsWith("//")) {
//			processedUrl = "http://"+one;
//		}else if(one.startsWith("/")){
//			processedUrl = url.endsWith("/")?url.substring(0, url.length()-1)+one:url+one;
//		}else {
//			processedUrl = url.endsWith("/")?url+one:url+"/"+one;
//		}
//		return processedUrl;
//	}
//	
//	private int getHashPos(String url, int num) {
//    	URLInfo info = new URLInfo(url);
//    	String domainName = info.getHostName();
//    	return Math.abs(domainName.hashCode()%num);
//    }
//	
//	private boolean checkModifiedTime(String url, long modifiedTime) {
//		if(timeTable.urlStored(url)) {
//			long accessedTime = timeTable.getLastAccessTime(url);
//			if(modifiedTime != 0 && modifiedTime < accessedTime) {
//				logger.info(url+": not modified");
//				return false;
//			}
//		}
//		return true;
//	}
//	
//	private boolean checkType(String url, String type) {
//		if(!type.equals("text/html")&&!type.equals("text/xml")&&!type.equals("application/xml")
//				&&!type.equals("text/html")&&!type.endsWith("xml")) {
//			logger.info(url+": type is not html or xml");
//			return false;
//		}
//		return true;
//	}
//	
//	private boolean checkSize(String url, int size) {
//		if(size > maxSize*Math.pow(2, 20)) {
//			logger.info(url+": size of the file is too big");
//			return false;
//		}
//		return true;
//	}
//	
//	private boolean emptySet(ConcurrentHashMap<Integer, ArrayList<String>> frontier) {
//		for(int idx: frontier.keySet()) {
//			if(!frontier.get(idx).isEmpty()) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//}
