package edu.upenn.cis.cis455.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import com.sleepycat.persist.model.*;

@Entity
public class User implements Serializable{

	@PrimaryKey
	private String id = new String();
	
	@SecondaryKey(relate = Relationship.MANY_TO_ONE)
	private String username = new String();

	private String password;
	private ArrayList<String> subscribed = new ArrayList<>();
	
	User(){
		setId(UUID.randomUUID().toString());
	}
	
	User(String username, String password){
		setUsername(username);
		setPassword(password);
		setId(UUID.randomUUID().toString());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ArrayList<String> getSubscribed() {
		return subscribed;
	}

	public void addSubscribedChannel(String channel) {
		if(!this.subscribed.contains(channel)) {
			this.subscribed.add(channel);
		}
	}

	
}
