package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageInterface;


import spark.Request;
import spark.Response;
import spark.Route;
import static spark.Spark.halt;

public class CreateChannelHandler implements Route {

    Storage db;

    public CreateChannelHandler(StorageInterface db){
        this.db = (Storage) db;
    }
    @Override
    public Object handle(Request request, Response response) throws Exception {
        String channelName = request.params("name");
        String XPath = request.queryParams("xpath");

        if (this.db.getAllChannels().contains(channelName)){
            String html = "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "<body>\n"
                    + "<p>" + "Channel " + channelName + " already exists." + "</p>\n"
                    + "<br>\n"
                    + "<a href=http://localhost:45555/>home\n"
                    + "</body>\n"
                    + "</html>";
            halt(409, html);
            return "";
        }
        else {
            String user = (String) request.attribute("user");
            try{
                this.db.addChannel(user, channelName, XPath);
            } catch (Exception e){
                halt(400, e.getMessage());
            }

            return "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "<body>\n"
                    + "<p>" + "Channel " + channelName + " created." + "</p>\n"
                    + "<br>\n"
                    + "<a href=http://localhost:45555/>home\n"
                    + "</body>\n"
                    + "</html>";
        }

    }
}
