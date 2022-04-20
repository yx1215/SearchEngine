package edu.upenn.cis.cis455.crawler;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import javax.xml.parsers.*;

import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

public class DOMParserBolt implements IRichBolt{

	static Logger log = LogManager.getLogger(DOMParserBolt.class);

	Fields schema = new Fields("event"); 
	
    String executorId = UUID.randomUUID().toString();

    private static OutputCollector collector;
    private static String type = null;

    public DOMParserBolt() {}
    
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
		String content = input.getStringByField("document");
		String documentId = input.getStringByField("url");
		type = input.getStringByField("type");
		Document document = getDocumentBasedOnType(type, content);
		Elements children = document.children();
		int level = 0;
		ArrayList<OccurrenceEvent> stack = new ArrayList<>();
		for(Element ele: children) {
			iterateElements(ele, documentId, level+1, type, stack);
		}
		for(OccurrenceEvent evt: stack) {
			collector.emit(new Values<Object>(evt));
		}
		Crawler.getItem().setWorking(false);
	}
	

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;		
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);		
	}

	@Override
	public Fields getSchema() {
		return schema; 
	}
	
	
	public static void iterateElements(Element node, String documentId, int level, String type, ArrayList<OccurrenceEvent> stack) {
		if(node==null) {
			return ;
		}
		OccurrenceEvent newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Open, node.tagName());
		newEvent.setDocumentUrl(documentId);
		newEvent.setLevel(level);
		newEvent.setDocType(type);
		stack.add(newEvent);
//		collector.emit(new Values<Object>(newEvent));
		
		if(node.ownText().trim().length()>0) {
			newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Text, node.ownText());
			newEvent.setDocumentUrl(documentId);
			newEvent.setLevel(level);
			newEvent.setDocType(type);
			stack.add(newEvent);
//			collector.emit(new Values<Object>(newEvent));		
		}
		if(node.children().size()>0) {
			Elements nodeList = node.children();
		    for (Element child: nodeList){	
				iterateElements(child, documentId, level+1, type, stack);
		    }
		}
	    newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Close, node.tagName());
		newEvent.setDocumentUrl(documentId);
		newEvent.setLevel(level);
		newEvent.setDocType(type);
		stack.add(newEvent);
//		collector.emit(new Values<Object>(newEvent));	
	}
	
	public static Document getDocumentBasedOnType(String type, String content) {
		Document document = null;
		if(type.contains("xml")) {
			document = Jsoup.parse(content, "", Parser.xmlParser());
		}else {
			document = Jsoup.parse(content);
		}
		return document;
	}
	

}
