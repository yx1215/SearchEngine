package edu.upenn.cis.cis455.xpathengine;

import java.util.HashMap;

public class XPathNode {

    private String pathElement;
    private final String queryId;
    private final int position;
    private final int relativePos;
    private final int level;
    private XPathNode nextNode = null;
    private String containTest = null;
    private String equalTest = null;

    public XPathNode(String queryId,
                     int position,
                     int relativePos,
                     int level){
        this.queryId = queryId;
        this.position = position;
        this.relativePos = relativePos;
        this.level = level;
    }

    public void setPathElement(String pathElement) {
        this.pathElement = pathElement;
    }

    public String getPathElement() {
        return pathElement;
    }

    public String getQueryId(){
        return this.queryId;
    }

    public int getPosition() {
        return position;
    }

    public int getRelativePos() {
        return relativePos;
    }

    public int getLevel() {
        return level;
    }

    public void setNextNode(XPathNode nextNode){
        this.nextNode = nextNode;
    }

    public XPathNode getNextNode() {
        return nextNode;
    }

    public void setContainTest(String containTest) {
        this.containTest = containTest;
    }

    public void setEqualTest(String equalTest) {
        this.equalTest = equalTest;
    }

    public boolean validText(String text){
        if (this.equalTest == null && this.containTest == null){
            return true;
        }
        if (text == null){
            return false;
        }
        if (this.equalTest != null){
            return text.equals(this.equalTest);
        }

        return text.contains(this.containTest);
    }

    @Override
    public String toString() {
        String tmp = null;
        if (nextNode != null){
            tmp = nextNode.getQueryId();
        }
        return "XPathNode{" +
                "pathElement='" + pathElement + '\'' +
                ", queryId='" + queryId + '\'' +
                ", position=" + position +
                ", relativePos=" + relativePos +
                ", level=" + level +
                ", nextNode=" + tmp +
                ", containTest='" + containTest + '\'' +
                ", equalTest='" + equalTest + '\'' +
                '}';
    }
}
