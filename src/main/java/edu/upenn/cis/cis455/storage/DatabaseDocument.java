package edu.upenn.cis.cis455.storage;

import java.io.Serializable;
import java.util.Date;

public class DatabaseDocument implements Serializable {

    private String content;
    private Date lastModified;
    private String contentType;

    public DatabaseDocument(String content, String contentType){
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

}
