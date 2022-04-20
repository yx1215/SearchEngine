package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Response;
import spark.Route;

public class MainPageHandler implements Route{

	public MainPageHandler() {}
	
	@Override
	public String handle(Request request, Response response) {
		response.status(200);
		response.type("text/html");
		try {
			String user = request.session().attribute("user");
			return "<html><h2>Here is the main page</h2><br><p>The User is "+user+
					"<a href='/logout'><button>logout</button></html>";
		}catch(Exception e) {
			response.redirect("/login");
			return "";
		}
	}

}
