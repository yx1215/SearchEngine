package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.documentStorage.Channel;
import edu.upenn.cis.cis455.storage.documentStorage.Storage;
import edu.upenn.cis.cis455.storage.documentStorage.StorageInterface;
import edu.upenn.cis.cis455.storage.documentStorage.User;

import spark.Request;
import spark.Response;
import spark.Route;

public class HomePageHandler implements Route {

    Storage db;

    public HomePageHandler(StorageInterface db){
        this.db = (Storage) db;
    }

    @Override
    public Object handle(Request req, Response res) {
        if (req.attribute("user") != null){
            String user = (String) req.attribute("user");
            return this.createHomepageHtml(user);
        }
        else{
            System.out.println("Not logged in, redirecting to login page.");
            res.redirect("/login-form.html");
            return "";
        }
    }

    private String createHomepageHtml(String username){
        String html = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<body>\n"
                + "<h1>Homepage</h1>\n"
                + "<p> Welcome! " + username + "</p>\n"
                + "<h1>Channels</h1>\n"
                + "<p>The following are the available channels in the system:</p>\n"
                + "<ul>\n"
                + "%s"
                + "</ul>\n"
                + "<br>\n"
                + "<p>The following are the channels you subscribed:</p>"
                + "<ul>\n"
                + "%s"
                + "</ul>\n"
                + "%s"
                + "</body>\n"
                + "</html>";

        StringBuilder builder = new StringBuilder();
        for (Object channelName : this.db.getAllChannels()){
            Channel channel = this.db.getChannel(channelName.toString());
            builder.append(String.format("<li><a href=/show?channel=%s>%s</a></li><br>\n", channelName, channelName + " : " + channel.getXPath()));
        }
//        System.out.println(builder);

        StringBuilder subscriptions = new StringBuilder();
        User user = this.db.getUser(username);
        for (String channelName: user.getSubscriptions()){
            Channel channel = this.db.getChannel(channelName);
            subscriptions.append(String.format("<li><a href=/show?channel=%s>%s</a></li><br>\n", channelName, channelName + " : " + channel.getXPath()));
        }
//        System.out.println(subscriptions);
        String adminString = "";

        if (user.getUserType() == User.Type.Admin){
            adminString = "<br><p>Hello admin</p>\n" +
                    "<form method=\"POST\" action=\"/startCrawler\">\n" +
                    "StartUrl:<input type=\"text\" name=\"startUrl\"/><br/>\n" +
                    "MaxSize: <input type=\"text\" name=\"size\"/><br/>\n" +
                    "MaxCount:<input type=\"text\" name=\"count\"/><br/>\n" +
                    "<input type=\"submit\" value=\"Start Crawl\"/>\n" +
                    "</form>\n";
        }

        return String.format(html, builder, subscriptions, adminString);

    }
}
