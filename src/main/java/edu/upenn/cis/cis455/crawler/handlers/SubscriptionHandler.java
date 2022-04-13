package edu.upenn.cis.cis455.crawler.handlers;


import spark.Request;
import spark.Response;
import spark.Route;
import static spark.Spark.halt;


import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageInterface;

import java.rmi.server.ExportException;

public class SubscriptionHandler implements Route {

    Storage db;
    public SubscriptionHandler(StorageInterface db){
        this.db = (Storage) db;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String channelName = request.queryParams("channel");
        String user = (String) request.attribute("user");
        try {
            this.db.subscribeChannel(user, channelName);
        } catch (Exception e){
            halt(400, "Unable to subscribe channel: " + channelName + " , " + e.getMessage());
        }
        response.redirect("/");
        return "";
    }
}
