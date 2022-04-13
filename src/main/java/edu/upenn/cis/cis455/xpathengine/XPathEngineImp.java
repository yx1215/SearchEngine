package edu.upenn.cis.cis455.xpathengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;

public class XPathEngineImp implements XPathEngine{

    private final HashMap<String, HashMap<String, ArrayList<XPathNode>>> matchTable = new HashMap<>();
    private int numQuery = 0;
    private boolean[] matched;
    private String[] expressions;
    public XPathEngineImp(){

    }
    /**
     * Sets the XPath expression(s) that are to be evaluated.
     *
     * @param expressions
     */
    @Override
    public void setXPaths(String[] expressions) {
        this.expressions = expressions;
        try {
            for (String exp:expressions){
                this.addXPath(exp);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        this.matched = new boolean[expressions.length];
        Arrays.fill(this.matched, Boolean.FALSE);
    }

    /**
     * Event driven pattern match.
     * <p>
     * Takes an event at a time as input
     *
     * @param event notification about something parsed, from a given document
     * @return bit vector of matches to XPaths
     */
    @Override
    public boolean[] evaluateEvent(OccurrenceEvent event, String type) {
        switch (event.getType()){
            case Open: case Text:
                if (type.equals("xml")){
                    handleOpen(event);
                }
                else if (type.equals("html")){
                    // do case-insensitive check
                    for (String elementPath : this.matchTable.keySet()){
                        if (elementPath.equalsIgnoreCase(event.getValue())){
                            OccurrenceEvent tmpEvent = new OccurrenceEvent(event.getType(), elementPath, event.getLevel(), event.getText());
                            handleOpen(tmpEvent);
                        }
                    }
                }
                break;
            case Close:
                if (type.equals("xml")){
                    handleClose(event);
                }
                else if (type.equals("html")){
                    // do case-insensitive check
                    for (String elementPath : this.matchTable.keySet()){
                        if (elementPath.equalsIgnoreCase(event.getValue())){
                            OccurrenceEvent tmpEvent = new OccurrenceEvent(event.getType(), elementPath, event.getLevel(), event.getText());
                            handleClose(tmpEvent);
                        }
                    }
                }
                break;
        }
        return this.matched;
    }

    // handle open/text event, if an open event occurs, we copy the corresponding node to CL if the level is correct.
    public void handleOpen(OccurrenceEvent event){
        HashMap<String, ArrayList<XPathNode>> elementNodes = this.matchTable.get(event.getValue());
        if (elementNodes != null){
            ArrayList<XPathNode> CL = elementNodes.get("CL");
            for (XPathNode node : CL){
                if (node.getLevel() == event.getLevel() && node.validText(event.getText())){
                    XPathNode nextNode = node.getNextNode();
                    if (nextNode == null){
                        int queryIndex = Integer.parseInt(node.getQueryId().split("-")[0]) - 1;
                        this.matched[queryIndex] = true;
                        continue;
                    }
                    ArrayList<XPathNode> nextNodeElementCL = this.matchTable.get(nextNode.getPathElement()).get("CL");
                    if (!nextNodeElementCL.contains(nextNode)){
                        nextNodeElementCL.add(nextNode);
                    }
                }
            }
        }
    }

    // handle close event, if the close event occurs, we remove the next node of the event
    // from the candidate list.
    public void handleClose(OccurrenceEvent event){
        String pathElement = event.getValue();
        if (this.matchTable.containsKey(pathElement)){
            ArrayList<XPathNode> CL = this.matchTable.get(pathElement).get("CL");
            for (XPathNode node : CL){
                if (node.getLevel() == event.getLevel()){
                    XPathNode nextNode = node.getNextNode();
                    if (nextNode != null){
                        String nextPathElement = nextNode.getPathElement();
                        ArrayList<XPathNode> nextCL = this.matchTable.get(nextPathElement).get("CL");
                        nextCL.remove(nextNode);
                    }
                }
            }
        }
    }

    /**
     * parse an XPath and create corresponding FSM
     * @param XPath xpath
     * @throws Exception, an exception is thrown for invalid XPath
     */
    public void addXPath(String XPath) throws Exception{
        if (!XPath.startsWith("/")){
            throw new InvalidPropertiesFormatException("XPath " + XPath + "should start with /");
        }
        XPath = XPath.substring(1);
        this.numQuery += 1;
        XPathNode prevNode = null;
        XPathNode curNode;
        int pos = 1;
        for (String element : XPath.split("/")){
            if (element.isEmpty()){
                throw new InvalidPropertiesFormatException("Does not support //");
            }
            if (element.isBlank()){
                throw new InvalidPropertiesFormatException("Blank string between two /.");
            }
            element = element.strip();
            String pathElement;
            curNode = new XPathNode(this.numQuery + "-" + pos,
                    pos,
                    pos == 1 ? 0 : 1,
                    pos);
            if (prevNode != null){
                prevNode.setNextNode(curNode);
            }
            // parse condition
            if (element.contains("[")){
                int conditionStart = element.indexOf("[");
                pathElement = element.substring(0, conditionStart).strip();
                if (!element.endsWith("]")){
                    throw new InvalidPropertiesFormatException("Condition String does not end with ] for element " + pathElement);
                }
                int conditionEnd = element.indexOf("]");
                String conditionSubstring = element.substring(conditionStart + 1, conditionEnd).strip();
                parseCondition(conditionSubstring, curNode);
            }
            else {
                pathElement = element;
            }

            // if the path element is invalid, throw exception
            if (!validXMLElement(pathElement)){
                throw new InvalidPropertiesFormatException("Invalid xml element: " + pathElement);
            }
            curNode.setPathElement(pathElement);

            HashMap<String, ArrayList<XPathNode>> tmp = this.matchTable.get(pathElement);
            if (tmp == null){
                tmp = new HashMap<>();
            }
            ArrayList<XPathNode> CL = tmp.get("CL");
            ArrayList<XPathNode> WL = tmp.get("WL");
            if (CL == null){
                CL = new ArrayList<>();
                WL = new ArrayList<>();
            }
            if (pos == 1){
                // add all nodes with pos=1 to candidate list
                CL.add(curNode);
            }
            else {
                WL.add(curNode);
            }
            tmp.put("CL", CL);
            tmp.put("WL", WL);
            this.matchTable.put(pathElement, tmp);
            prevNode = curNode;
            pos = pos + 1;
        }
    }

    public HashMap<String, HashMap<String, ArrayList<XPathNode>>> getMatchTable() {
        return matchTable;
    }

    public boolean[] getMatchResult() {
        return matched;
    }

    public static void parseCondition(String condition, XPathNode node) throws InvalidPropertiesFormatException {
        String testString;
        String testType;
        if (condition.startsWith("text()")){
            // case [text() = "..."]
            testType = "equal";
            if (condition.split("=").length != 2){
                throw new InvalidPropertiesFormatException("Invalid number of '=' for condition substring: " + condition);
            }
            testString = condition.split("=")[1].strip();
        }
        else if (condition.startsWith("contains") && condition.endsWith(")")){
            // case [contains(text(), "...")]
            testType = "contain";
            if (!condition.substring(8).strip().startsWith("(")){
                throw new InvalidPropertiesFormatException("Missing ( for contain test.");
            }
            condition = condition.substring(8).strip().substring(1);
            if (condition.split(",").length != 2){
                throw new InvalidPropertiesFormatException("Invalid condition substring: " + condition);
            }
            if (!condition.split(",")[0].strip().equals("text()")){
                throw new InvalidPropertiesFormatException("Invalid contain test type: " + condition);
            }

            testString = condition.split(",")[1].strip();

            testString = testString.substring(0, testString.length() - 1).strip();
        }
        else {
            throw new InvalidPropertiesFormatException("Invalid condition substring: " + condition + " , neither contain nor equal test.");
        }

        if (!testString.startsWith("\"") || !testString.endsWith("\"")){
            throw new InvalidPropertiesFormatException("Missing \" around text for condition substring: " + condition);
        }
        // remove quotation marks
        testString = testString.substring(1, testString.length() - 1);
        if (testType.equals("equal")){
            node.setEqualTest(testString);
        }
        else {
            node.setContainTest(testString);
        }
    }

    public static boolean validXMLElement(String element){

        char[] elements = element.toCharArray();

        if (element.isEmpty()){
            return false;
        }

        // Element names must start with a letter or underscore
        if (!Character.isAlphabetic(elements[0]) && elements[0] != '_'){
            return false;
        }

        // Element names cannot start with the letters xml (or XML, or Xml, etc)
        if (element.length() > 3 && element.substring(0, 3).equalsIgnoreCase("xml")){
            return false;
        }

        // Element names can only contain letters, digits, hyphens, underscores, and periods
        for (char e : elements){
            if (
                    !Character.isAlphabetic(e) &&
                    !Character.isDigit(e) &&
                    e != '-' && e != '_' && e != '.'
            ) {
                return false;
            }
        }

        // Element names cannot contain spaces
        for (char e : elements){
            if (e == ' ') {
                return false;
            }
        }

        return true;
    }
}
