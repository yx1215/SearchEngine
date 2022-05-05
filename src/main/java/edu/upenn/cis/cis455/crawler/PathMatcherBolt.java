//package edu.upenn.cis.cis455.crawler;
//
//import java.util.Map;
//import java.util.UUID;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import edu.upenn.cis.cis455.storage.Storage;
//import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
//import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
//import edu.upenn.cis.cis455.xpathengine.XPathEngineImpl;
//import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
//import edu.upenn.cis.stormlite.TopologyContext;
//import edu.upenn.cis.stormlite.bolt.IRichBolt;
//import edu.upenn.cis.stormlite.bolt.OutputCollector;
//import edu.upenn.cis.stormlite.routers.IStreamRouter;
//import edu.upenn.cis.stormlite.tuple.Fields;
//import edu.upenn.cis.stormlite.tuple.Tuple;
//
//public class PathMatcherBolt implements IRichBolt{
//	
//	static Logger log = LogManager.getLogger(DOMParserBolt.class);
//
//	Fields schema = new Fields("documentURL","event", "type"); 
//	
//    String executorId = UUID.randomUUID().toString();
//
//    private OutputCollector collector;
//    private String type = null;
//    private XPathEngineImpl engine = null;
//    private Storage db = null;
//    private String[] xpath = null;
//
//    public PathMatcherBolt() {}
//    
//	@Override
//	public String getExecutorId() {
//		return executorId;
//	}
//
//	@Override
//	public void declareOutputFields(OutputFieldsDeclarer declarer) {
//        declarer.declare(schema);		
//	}
//
//	@Override
//	public void cleanup() {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
//        this.collector = collector;	
//        this.engine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
//        this.db = (Storage) Crawler.getItem().db;
//        String[] names = this.db.getAllChannelName();
//        xpath = new String[names.length];
//        for(int i = 0; i< names.length; i++) {
//        	xpath[i] = this.db.getChannelByName(names[i]).getPattern();
//        }
//        this.engine.setXPaths(xpath);
//	}
//	
//	@Override
//	public void execute(Tuple input) {
//		Crawler.getItem().setWorking(true);
//		OccurrenceEvent event = (OccurrenceEvent) input.getObjectByField("event");
//		try{
//			boolean[] result = this.engine.evaluateEvent(event);
//			for(int i = 0; i<result.length; i++) {
//				if(result[i]==true) {
//					//store in the db
//					this.db.addUrlToChannel(this.xpath[i], event.getDocumentUrl());
//				}
//			}
//		}catch(Exception e) {
//			System.err.println("DataBase is closed");
//		}
//		Crawler.getItem().setWorking(false);
//	}
//
//	@Override
//	public void setRouter(IStreamRouter router) {
//		this.collector.setRouter(router);		
//	}
//
//	@Override
//	public Fields getSchema() {
//		return schema;
//	}
//
//}
