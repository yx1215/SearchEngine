package edu.upenn.cis.cis455.storage;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class DomainAccess implements Serializable{
	@PrimaryKey
	private int domainId = 0;
	
	@SecondaryKey(relate = Relationship.ONE_TO_ONE)
	private String domainName = new String();
	
	private long lastAccessTime = 0;
	
	private int delay = 0;
	
	DomainAccess(){
		setDomainId(Sequence.nextValue());
		setLastAccessTime(System.currentTimeMillis());
	}
	
	DomainAccess(String domainName, int delay){
		setDomainId(Sequence.nextValue());
		setLastAccessTime(System.currentTimeMillis());
		setDelay(delay);
		setDomainName(domainName);
	}

	public int getDomainId() {
		return domainId;
	}

	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}
	

}
