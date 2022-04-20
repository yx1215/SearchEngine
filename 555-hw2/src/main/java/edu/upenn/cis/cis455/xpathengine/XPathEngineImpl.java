package edu.upenn.cis.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;

public class XPathEngineImpl implements XPathEngine{
	
	private String[] xpath = null;
	private HashMap<String, QueryIndex> queryMap = new HashMap<>();
	private HashMap<String, ArrayList<Integer>> statusMap = new HashMap<>();//store url and pass xpathId

	@Override
	public void setXPaths(String[] expressions) {
		this.xpath = expressions;
	}

	@Override
	public boolean[] evaluateEvent(OccurrenceEvent event) {
//		String eventItem = event.getValue();
		String documentId = event.getDocumentUrl();
//		String type = event.getDocType();
		QueryIndex indexTable = null;
		ArrayList<Integer> status = null; // xpath and passed or not
		
		//find the query table
		if(queryMap.containsKey(documentId)) {
			indexTable = queryMap.get(documentId);
		}else {
			indexTable = new QueryIndex(xpath, documentId, event.getDocType());
			queryMap.put(documentId, indexTable);
		}
		if(statusMap.containsKey(documentId)) {
			status = statusMap.get(documentId);
		}
		if(event.getType().equals(OccurrenceEvent.Type.Open)) {
			//if first encounter, just initialize a new status map
			if(status==null) {
				status = new ArrayList<>();
				statusMap.put(documentId, status);
			}
			EventHandler.startEventHandler(event, indexTable, status);
		}else if(event.getType().equals(OccurrenceEvent.Type.Text)) {
			if(status==null) {
				status = new ArrayList<>();
				statusMap.put(documentId, status);
			}
			EventHandler.charEventHandler(event, indexTable, status);
		}else {
			boolean result = EventHandler.endEventHandler(event, indexTable, status);
			// if the document closes all elements, remove the status map and index map
			if(result) {
				queryMap.remove(documentId);
				statusMap.remove(documentId);
			}
		}
		boolean[] matchedXPath = new boolean[xpath.length];
		for(int qid: status) {
			matchedXPath[qid]=true;
		}
		return matchedXPath;
	}
	
//	public static void main(String[] args) {
//		XPathEngineImpl engine = new XPathEngineImpl();
//		String[] expressions = new String[2];
//		expressions[0]="/a[text()  =    'xxx']/b";
//		expressions[1]="/a/c[text()  =    '']";
//		engine.setXPaths(expressions);
//		OccurrenceEvent newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Open, "a");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(1);
//		engine.evaluateEvent(newEvent);
//		
//		newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Text, "xxx");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(1);
//		engine.evaluateEvent(newEvent);
//		
//		newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Open, "b");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(2);
//		engine.evaluateEvent(newEvent);
//		
//		newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Text, "hello world");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(2);
//		engine.evaluateEvent(newEvent);
//		
//		newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Close, "b");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(2);
//		engine.evaluateEvent(newEvent);
//		
//		
//		
//		newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Open, "c");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(2);
//		engine.evaluateEvent(newEvent);
//		newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Close, "c");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(2);
//		engine.evaluateEvent(newEvent);
//		newEvent = new OccurrenceEvent(OccurrenceEvent.Type.Close, "a");
//		newEvent.setDocumentUrl("qqq");
//		newEvent.setLevel(1);
//		boolean[] result = engine.evaluateEvent(newEvent);
//		System.out.print(result[0]+"   "+result[1]);
//	}

}
