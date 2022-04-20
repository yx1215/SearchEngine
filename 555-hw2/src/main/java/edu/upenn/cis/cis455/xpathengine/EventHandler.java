package edu.upenn.cis.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventHandler {
	
	public static boolean startEventHandler(OccurrenceEvent event, QueryIndex qidx, ArrayList<Integer> status) {
		 HashMap<String, HashMap<String, ConcurrentLinkedQueue<PathNode>>> qTable = qidx.getElementTable();
		 String name = event.getDocType().equals("text/html")?event.getValue().toLowerCase():event.getValue();
		 ConcurrentLinkedQueue<PathNode> nodeList = new ConcurrentLinkedQueue<>();
		 if(event.getDocType().equals("text/html")) { 
			 for(String key: qTable.keySet()) { if(key.toLowerCase().equals(name)) { nodeList.addAll(qTable.get(key).get("CL")); } }
		 }else { nodeList = qTable.get(name).get("CL"); }
		 for(PathNode node: nodeList) {
			 //find the element with correct level
			 if(node.getLevel() != event.getLevel()) {
				 continue;
			 }
			 //if it is the final, records that this query(xpath) is vlaid for this document
			 if(node.isFinal()) {
				 if(node.getTextType().equals("none")) {
					 status.add(node.getQid());
				 }
			 }else {
				 PathNode nextNode = node.getNextNode();
				 qTable.get(nextNode.getElement()).get("CL").add(nextNode);
			 }
		 }
		 return true;
	}
	
	public static boolean endEventHandler(OccurrenceEvent event, QueryIndex qidx, ArrayList<Integer> status) {
		HashMap<String, HashMap<String, ConcurrentLinkedQueue<PathNode>>> qTable = qidx.getElementTable();
		String name = event.getDocType().equals("text/html")?event.getValue().toLowerCase():event.getValue();
		HashMap<String, ConcurrentLinkedQueue<PathNode>> map = qTable.get(name);;
		if(event.getDocType().equals("text/html")) { for(String key: qTable.keySet()) { if(key.toLowerCase().equals(name)) { map = qTable.get(key);} } }else { map = qTable.get(name); }
		ConcurrentLinkedQueue<PathNode> CL = new ConcurrentLinkedQueue<>();
		 if(event.getDocType().equals("text/html")) { 
			 for(String key: qTable.keySet()) { if(key.toLowerCase().equals(name)) { CL.addAll(qTable.get(key).get("CL")); } }
		 }else { CL = qTable.get(name).get("CL"); }
		for(PathNode node: CL) {
			// if we find the node in same level, remove it
			if (node.getLevel()==event.getLevel()) {
				qidx.getElementTable().get(event.getValue()).get("CL").remove(node);
				if(event.getLevel() == 1) {
					return true;
				}
				break;
			}
		}
		return false;
	}
	public static boolean charEventHandler(OccurrenceEvent event, QueryIndex qidx, ArrayList<Integer> status) {
		HashMap<String, HashMap<String, ConcurrentLinkedQueue<PathNode>>> qTable = qidx.getElementTable();
		String data = event.getValue();
		for(String ele: qidx.getElementTable().keySet()) {
			ConcurrentLinkedQueue<PathNode> CL = qidx.getElementTable().get(ele).get("CL");
			for (PathNode node: CL) {
				if(node.getLevel()==event.getLevel() && matchTxt(node, data)) {
					if(node.isFinal()) {
						status.add(node.getQid());
						return true;
					}
					PathNode nextNode = node.getNextNode();
					qTable.get(nextNode.getElement()).get("CL").add(nextNode);
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean matchTxt(PathNode node, String data) {
		String txtType = node.getTextType(); 
		if(txtType.equals("none")) {
			return false;
		}else if(txtType.equals("equal")) {
			return data.equals(node.getContent());
		}else {
			return data.contains(node.getContent());
		}
	}

}
