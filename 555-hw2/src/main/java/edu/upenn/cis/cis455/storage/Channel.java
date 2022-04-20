package edu.upenn.cis.cis455.storage;

import java.io.Serializable;
import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class Channel implements Serializable{
	
	@PrimaryKey
	private int channelId = 0;
	
	@SecondaryKey(relate = Relationship.ONE_TO_ONE)
	private String channelName = new String();
	
	@SecondaryKey(relate = Relationship.MANY_TO_ONE)
	private String pattern = new String();
	
	private ArrayList<String> url = new ArrayList<>();
	private String creator = new String();
	private ArrayList<String> subscribers = new ArrayList<>();

	public Channel() {
		setChannelId(Sequence.nextValue());
	}

	public Channel(String name, String pattern, String user) {
		setChannelId(Sequence.nextValue());
		setChannelName(name);
		setPattern(pattern);
		setCreator(user);
	}
	
	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public ArrayList<String> getUrl() {
		return url;
	}

	public void addUrl(String addUrl) {
		if(!this.url.contains(addUrl)) {
			this.url.add(addUrl);
			System.out.print(this.url.get(0));
		}
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	

}
