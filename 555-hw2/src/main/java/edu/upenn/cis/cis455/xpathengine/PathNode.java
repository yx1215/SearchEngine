package edu.upenn.cis.cis455.xpathengine;

public class PathNode {
	
	private String element = null;
	private PathNode nextNode = null;
	private boolean isFinal = false;
	private int level = 0;
	private int qid = -1;
	private String content = "";
	private String textType = "";
	
	PathNode(String element){
		this.element = element;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public PathNode getNextNode() {
		return nextNode;
	}

	public void setNextNode(PathNode nextNode) {
		this.nextNode = nextNode;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public int getQid() {
		return qid;
	}

	public void setQid(int qid) {
		this.qid = qid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTextType() {
		return textType;
	}

	public void setTextType(String textType) {
		this.textType = textType;
	}

}
