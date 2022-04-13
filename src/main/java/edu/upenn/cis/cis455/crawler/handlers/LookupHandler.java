package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.DatabaseDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import spark.Request;
import spark.Route;
import spark.Response;
import static spark.Spark.halt;



public class LookupHandler implements Route {
    StorageInterface db;
    final static Logger logger = LogManager.getLogger(LookupHandler.class);
    public LookupHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public Object handle(Request request, Response response) {

        String url = request.queryParams("url");
        if (url == null){
            url = "";
        }
        else {
            URLInfo info = new URLInfo(url);
            if (info.getHostName() != null){
                url = info.toString();
            }
        }

        logger.info("Fetching " + url);
        DatabaseDocument doc;
        if (url.startsWith("http://") || url.startsWith("https://")){
            doc = this.db.getDocument(url);
        }
        else {
            String httpUrl = new URLInfo("http://" + url).toString();
            doc = this.db.getDocument(httpUrl);
            if (doc == null){
                String httpsUrl = new URLInfo("https://" + url).toString();
                doc = this.db.getDocument(httpsUrl);
            }
        }

        if (doc == null){
            halt(404, "Url: " + url + " not found.");
            return "";
        }
        else {
            response.type(doc.getContentType());
            return doc.getContent();
        }
    }
}
