package edu.upenn.cis.cis455.xpathengine;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class SAXHandler extends DefaultHandler {

    private final StringBuilder builder = new StringBuilder();
    private final ArrayList<OccurrenceEvent> events;
    private int curLevel = 0;
    private ArrayList<String> tags = new ArrayList<>();

    public SAXHandler(ArrayList<OccurrenceEvent> events){
        this.events = events;
    }

    @Override
    public void startDocument(){
        OccurrenceEvent event = new OccurrenceEvent(OccurrenceEvent.Type.Open, "START", 0);
        this.events.add(event);
//        System.out.println("Start document");
    }

    @Override
    public void endDocument(){
        OccurrenceEvent event = new OccurrenceEvent(OccurrenceEvent.Type.Close, "END", 0);
        this.events.add(event);
//        System.out.println("End document");
    }

    @Override
    public void startElement(
           String uri,
           String localName,
           String qName,
           Attributes attributes
    ){

        OccurrenceEvent event;
        if (builder.length() != 0){
            // System.out.println("Text: " + builder);
            // if the string builder is not empty when we encounter a new open tag,
            // it means that there is text for the last open tag
            String lastTag = this.tags.get(this.tags.size() - 1);

            event = new OccurrenceEvent(OccurrenceEvent.Type.Text, lastTag, this.curLevel);
            event.setText(builder.toString());
            this.events.add(event);
            builder.setLength(0);
        }
        // create the open event
        curLevel += 1;
        this.tags.add(qName);
        event = new OccurrenceEvent(OccurrenceEvent.Type.Open, qName, this.curLevel);
        this.events.add(event);
//        System.out.println("Start Element: " + qName);
    }

    @Override
    public void endElement(
            String uri,
            String localName,
            String qName
    ){
        OccurrenceEvent event;
        if (builder.length() != 0){
//            System.out.println("Text: " + builder);
            // if the string builder is not empty when we encounter the close tag
            // it means there is text for the current tag
            event = new OccurrenceEvent(OccurrenceEvent.Type.Text, qName, this.curLevel);
            event.setText(builder.toString());
            this.events.add(event);
            builder.setLength(0);
        }

        // create the close event
        event = new OccurrenceEvent(OccurrenceEvent.Type.Close, qName, this.curLevel);
        this.tags.remove(this.tags.size() - 1);
        this.events.add(event);
//        System.out.println("End Element: " + qName);
        curLevel -= 1;
    }

    @Override
    public void characters(char[] ch, int start, int length){
        builder.append(new String(ch, start, length).strip());
    }
}
