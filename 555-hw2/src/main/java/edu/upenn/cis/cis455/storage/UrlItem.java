package edu.upenn.cis.cis455.storage;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class UrlItem implements Serializable{
	@PrimaryKey
	private int Id = 0;
	
	@SecondaryKey(relate = Relationship.ONE_TO_ONE)
	private String url = new String();
	
	private long lastAccessTime = 0;
	
	public UrlItem() {
		setId(Sequence.nextValue());
	}
	
	public UrlItem(String url, long time) {
		setId(Sequence.nextValue());
		setUrl(url);
		setLastAccessTime(time);
	}
	

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
}
