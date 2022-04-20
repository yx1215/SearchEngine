package edu.upenn.cis.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueryIndex {
	private static HashMap<String, HashMap<String, ConcurrentLinkedQueue<PathNode>>> elementTable = new HashMap<>();;
	private String documentUrl = null;

	public HashMap<String, HashMap<String, ConcurrentLinkedQueue<PathNode>>> getElementTable() {
		return elementTable;
	}

	public void setElementTable(HashMap<String, HashMap<String, ConcurrentLinkedQueue<PathNode>>> elementTable) {
		this.elementTable = elementTable;
	}
	
	QueryIndex(String[] xpaths, String documentUrl, String type){
		this.setElementTable(buildNewQueryIndexTable(xpaths, type));
		this.documentUrl = documentUrl;
	}
	
	public static HashMap<String, HashMap<String, ConcurrentLinkedQueue<PathNode>>> buildNewQueryIndexTable(String[] xpaths, String type){
		if(type.equals("text/html")) {
			for(int i=0; i<xpaths.length; i++) {
				xpaths[i] = xpaths[i];
			}
		}
		for(int i = 0; i<xpaths.length; i++) {
			ArrayList<PathNode> pathList = XPathParser.parseXPath(xpaths[i]);
			addXPathToTable(i, pathList);
		}
		return elementTable;
	}
	
	public static void addXPathToTable(int index, ArrayList<PathNode> pathList) {
		for(int i = 0; i< pathList.size(); i++) {
			PathNode node = pathList.get(i);
			node.setQid(index);
			String ele = node.getElement();
			if(!elementTable.containsKey(ele)) {
				elementTable.put(ele, new HashMap<String, ConcurrentLinkedQueue<PathNode>>());
				elementTable.get(ele).put("CL", new ConcurrentLinkedQueue<>());
				elementTable.get(ele).put("WL", new ConcurrentLinkedQueue<>());
			}
			if(i==0) {
				elementTable.get(ele).get("CL").add(node);
			}else {
				elementTable.get(ele).get("WL").add(node);
			}
		}
	}
	public ConcurrentLinkedQueue<PathNode> getCL(String name){
		return elementTable.get(name).get("CL");
	}
	
	public ConcurrentLinkedQueue<PathNode> getWL(String name){
		return elementTable.get(name).get("WL");
	}

}
