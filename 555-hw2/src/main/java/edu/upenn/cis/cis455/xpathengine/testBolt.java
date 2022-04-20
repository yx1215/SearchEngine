package edu.upenn.cis.cis455.xpathengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.RobotsHandler;
import edu.upenn.cis.cis455.crawler.RuleItem;
import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

public class testBolt {

	static Logger log = LogManager.getLogger(testBolt.class);
	public static ArrayList<OccurrenceEvent> eventArray = new ArrayList<>();
	
	public static void main(String[] args) {
		ArrayList<OccurrenceEvent> res = testBolt.execute();
		System.out.print(res.size());
	}
	public static ArrayList<OccurrenceEvent> execute() {
		
		String documentId = "https://crawltest.cis.upenn.edu/cnn/cnn_law.rss.xml";
		String content = getContent(documentId);
		Document document = Jsoup.parse(content);
		Elements children = document.children();
		int level = 0;
		for(Element ele: children) {
			iterateElements(ele, documentId, level+1);
		}
		return eventArray;
	}
	
	
	public static void iterateElements(Element node, String documentId, int level) {
		OccurrenceEvent newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Open, node.tagName());
		newEvent.setDocumentUrl(documentId);
		newEvent.setLevel(level);
		eventArray.add(newEvent);
		
		if(node.ownText().trim().length()>0) {
			newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Text, node.ownText());
			newEvent.setDocumentUrl(documentId);
			newEvent.setLevel(level);
			eventArray.add(newEvent);		
		}
		
	    Elements nodeList = node.children();
	    for (Element child: nodeList){	
			iterateElements(child, documentId, level+1);
	    }
	    newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Close, node.tagName());
		newEvent.setDocumentUrl(documentId);
		newEvent.setLevel(level);
		eventArray.add(newEvent);	
	}
	
	public static String getContent(String url) {
		try {
			URL urlObj = new URL(url);
			HttpURLConnection httpCon = null;
			HttpsURLConnection httpsCon = null;
			String result = null;
			if(url.startsWith("https")) {
				httpsCon = (HttpsURLConnection) urlObj.openConnection();
				httpsCon.setRequestMethod("GET");
				httpsCon.setRequestProperty("User-Agent", "cis455crawler");
				httpsCon.connect();
			}else {
				httpCon = (HttpURLConnection) urlObj.openConnection();
				httpCon.setRequestMethod("GET");
				httpCon.setRequestProperty("User-Agent", "cis455crawler");
				httpCon.connect();
			}
			
			int responseCode = httpCon!=null ? httpCon.getResponseCode(): httpsCon.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
			    System.out.println("Server returned response code " + responseCode + ". Download failed.");
			}else {
				InputStream inputStream = httpCon != null ? httpCon.getInputStream() : httpsCon.getInputStream();
				result = convertInputStreamToString(inputStream);
			}
			if(httpCon != null) {
				httpCon.disconnect();
			}else {
				httpsCon.disconnect();
			}
			return result;
		} catch (MalformedURLException e) {
			System.out.println("not valid url: "+url);
		} catch (IOException e) {
			System.out.println("The connection goes wrong.");
		}
		return null;
	}
	
	private static String convertInputStreamToString(InputStream inputStream) {
		String text = new BufferedReader(
			      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\r\n"));
		return text;
	}
}
