package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.Channel;
import edu.upenn.cis.cis455.storage.DatabaseDocument;
import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageInterface;


import spark.Request;
import spark.Response;
import spark.Route;
import static spark.Spark.halt;

import java.text.SimpleDateFormat;
import java.util.Locale;



public class ShowHandler implements Route {

    Storage db;

    public ShowHandler(StorageInterface db){
        this.db = (Storage) db;
    }
    @Override
    public Object handle(Request req, Response res) throws Exception {
        String channelName = req.queryParams("channel");
        Channel channel = this.db.getChannel(channelName);
        if (channel == null){
            halt(404, "Channel " + channelName + " does not exist.");
            return "";
        }
        else {
            return createChannelHtml(channelName, channel);
        }
    }

    private String createChannelHtml(String channelName, Channel channel){
        String html = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<body>\n"
                + "<h1>Channel Content</h1>\n"
                + "<div class=\"channelheader\">\n"
                + "Channel name: %s, created by: %s\n"
                + "</div>\n"
                + "<p>Document Matched:</p>\n"
                + "%s"
                + "</body>\n"
                + "</html>";

        StringBuilder builder = new StringBuilder();
        for (String url : channel.getMatchedUrls()){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.ENGLISH);
            DatabaseDocument doc = this.db.getDocument(url);
            builder.append("Crawled on: ").append(formatter.format(doc.getLastModified())).append("\n");
            builder.append("<br><br>");
            builder.append("Location: ").append(url).append("\n");
            builder.append("<br><br>");
            builder.append("<div class=\"document\">\n").append(doc.getContent()).append("</div>\n");
            builder.append("<br><br>");
        }

        return String.format(html, channelName, channel.getCreator(), builder);

    }
}
