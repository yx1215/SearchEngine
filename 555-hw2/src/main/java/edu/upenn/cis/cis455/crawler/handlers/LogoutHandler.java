package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandler implements Route{

	public LogoutHandler(){}
	
	@Override
	public Object handle(Request request, Response response) throws Exception {
		request.session().invalidate();
		response.redirect("/login");
		return null;
	}

}
