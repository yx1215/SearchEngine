package edu.upenn.cis.cis455.storage;

import java.io.Serializable;
import java.util.ArrayList;

public class Channel implements Serializable {

    private final String channelName;
    private final String XPath;
    private final String creator;
    private final ArrayList<String> matchedUrls = new ArrayList<>();

    public Channel(String channelName, String XPath, String creator){
        this.channelName = channelName;
        this.XPath = XPath;
        this.creator = creator;
    }

    public ArrayList<String> getMatchedUrls() {
        return matchedUrls;
    }

    public String getXPath() {
        return XPath;
    }

    public String getCreator() {
        return creator;
    }

    public String getChannelName() {
        return channelName;
    }

    public void addUrl(String url){
        if (!this.matchedUrls.contains(url)){
            this.matchedUrls.add(url);
        }
        else {
            System.out.println("url: " + url + " already exists in channel " + this.channelName);
        }
    }
}
