package edu.upenn.cis.cis455.storage;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Document implements Serializable{
	@PrimaryKey
	private int docId = 0;
	
	@SecondaryKey(relate = Relationship.ONE_TO_ONE)
	private String url = new String();
	
	@SecondaryKey(relate = Relationship.ONE_TO_ONE)
	private String content = new String();
	
	private long accessTime = 0;
	private String type = new String();

	public Document() {
		setDocId(Sequence.nextValue());
		setAccessTime(System.currentTimeMillis());
	}

	public Document(String word, String docData) {
		setDocId(Sequence.nextValue());
		setUrl(word);
		setContent(docData);
		setAccessTime(System.currentTimeMillis());
	}
	
	public Document(String word, String docData, String type) {
		setDocId(Sequence.nextValue());
		setUrl(word);
		setContent(docData);
		setType(type);
		setAccessTime(System.currentTimeMillis());
	}
	
	public long getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(long accessTime) {
		this.accessTime = accessTime;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
