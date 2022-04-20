package edu.upenn.cis.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.stormlite.tuple.Values;

public class test {
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
	public static void iterateElements(Node node, int level, ArrayList<String> stack) {
		if(node instanceof TextNode) {
			return ;
		}
		if(!node.nodeName().startsWith("#")) {
			System.out.println(" in level "+level+" and name is"+node.nodeName());
//			stack.add(node.)
		}
		
		List<Node> nodeList = node.childNodes();
	    for (Node child: nodeList){	
	    	iterateElements(child, level+1, stack);
	    }
	    if(!node.nodeName().startsWith("#")) {
			System.out.println("end in level "+level+" and name is"+node.nodeName());
		}

	}
	public static void main(String args[]) {
		String content  = getContent("https://crawltest.cis.upenn.edu/marie/tpc/nation.xml");
//		if(type.contains("xml")) {
//			Document document = Jsoup.parse(content, "", Parser.xmlParser());
//		}
		System.out.println(content);
//		Document document = Jsoup.parse(content);
		Document document = Jsoup.parse(content, "", Parser.xmlParser());
		List<Node> children = document.childNodes();
		int level = 0;
		for(Node ele: children) {
			iterateElements(ele, level+1, new ArrayList<String>());
		}
	}
}
