package edu.upenn.cis.cis455.storage;

import java.io.Serializable;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class CheckSum implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6229697894089286847L;

	@PrimaryKey
	private int id = 0;
	
	@SecondaryKey(relate = Relationship.ONE_TO_ONE)
	private String contentHash = new String();
	
	CheckSum(){
		setId(Sequence.nextValue());
	}
	
	CheckSum(String contentHash){
		setId(Sequence.nextValue());
		setContentHash(contentHash);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContentHash() {
		return contentHash;
	}

	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}
}
