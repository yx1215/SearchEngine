package edu.upenn.cis.cis455.xpathengine;

import java.util.ArrayList;

public class XPathParser {
	
	public static ArrayList<PathNode> parseXPath(String path) {
		ArrayList<PathNode> pathList= new ArrayList<>();
		String[] eles = path.split("/");
		int level = 1;
		PathNode node = null;
		for(String ele: eles) {
			ele = ele.trim();
			if(ele.length()!=0) {
				String type = checkText(ele);
				if(type.equals("none")) {
					node = new PathNode(ele);
				}else{
					String text = getData(ele);
					ele = getName(ele);
					node = new PathNode(ele);
					node.setContent(text);
				}
				node.setTextType(type);
				node.setLevel(level);
				pathList.add(node);
				level+=1;
				
			}
		}
		return linkPathNode(pathList);
	}
	
	private static ArrayList<PathNode> linkPathNode(ArrayList<PathNode> pathList) {
		if(pathList.size()==0) {
			return pathList;
		}
		for(int i = 0; i<pathList.size()-1; i++) {
			pathList.get(i).setNextNode(pathList.get(i+1));
		}
		pathList.get(pathList.size()-1).setFinal(true);
		return pathList;
	}
	
	private static String checkText(String ele) {
		
		if(ele.replace(" ", "").contains("[contains(text(),")) {
			return "contains";
		}else if(ele.replace(" ", "").contains("[text()=")) {
			return "equal";
		}
		return "none";
	}
	private static String getName(String ele) {
		return ele.substring(0, ele.indexOf("["));
	}
	private static String getData(String ele) {
		String info = "";
		if(ele.contains("contains")) {
			info = ele.split(",")[1].trim().replace("]", "").replace(")", "");
		}else{
			info = ele.split("=")[1].trim().replace("]", "");
		}
		return info.replace("'", "").replace("\"", "");
	}

}
