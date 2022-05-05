package edu.upenn.cis.cis455.crawler.handlers;

import com.sleepycat.je.Database;

import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageInterface;
import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Session;

public class RegistrationHandler implements Route{
	StorageInterface db = null;
	
	public RegistrationHandler(StorageInterface database){
		this.db = database;
	}

	@Override
	public Object handle(Request request, Response response) throws HaltException {
		// TODO Auto-generated method stub
		String username = request.queryParams("username");
		String password = request.queryParams("password"); 
		//if not exists, add user
		;
		if (this.db.addUser(username, password)==1) {
			System.err.println("Logged in!"); 
            Session session = request.session(); 
            session.maxInactiveInterval(300);
            session.attribute("user", username);
            session.attribute("password", password);
            response.redirect("/");
		}else {
			System.err.println("Username exists!");
			response.redirect("/register.html");
		}
		return null;
	}
	
	
}
