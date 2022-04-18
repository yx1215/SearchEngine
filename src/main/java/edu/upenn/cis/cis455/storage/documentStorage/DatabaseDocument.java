package edu.upenn.cis.cis455.storage.documentStorage;

import java.io.Serializable;
import java.util.Date;

public class DatabaseDocument implements Serializable {

    private String url;
    private String content;
    private Date lastModified;
    private String contentType;

    public DatabaseDocument(String url, String content, String contentType){
        this.url = url;
        this.content = content;
        this.lastModified = new Date();
        this.contentType = contentType;
    }

    public String getContent(){
        return this.content;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public String getContentType(){
        return this.contentType;
    }

    public String getUrl() {
        return url;
    }
}
