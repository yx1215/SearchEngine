package edu.upenn.cis.cis455.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class Accessor {
	
	PrimaryIndex<String, User> userById;
	SecondaryIndex<String, String, User> userByName;
	
	public Accessor(EntityStore store) throws DatabaseException{
		userById = store.getPrimaryIndex(String.class, User.class);
	}

}
