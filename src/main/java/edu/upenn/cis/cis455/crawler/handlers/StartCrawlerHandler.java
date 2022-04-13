package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.Crawler;


import spark.Request;
import spark.Route;
import spark.Response;
import static spark.Spark.halt;


import edu.upenn.cis.cis455.storage.User;
import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StartCrawlerHandler implements Route {

    Storage db;
    final static Logger logger = LogManager.getLogger(StartCrawlerHandler.class);

    public static Crawler crawler = null;
    public static Thread crawlerThread = null;

    public StartCrawlerHandler(StorageInterface db){
        this.db = (Storage) db;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String username = request.attribute("user").toString();
        User user = this.db.getUser(username);
        if (user.getUserType() == User.Type.Admin){
            String startUrl = request.queryParams("startUrl");
            String size = request.queryParams("size");
            String count = request.queryParams("count");
            Storage tmpDb = (Storage) StorageFactory.getDatabaseInstance(this.db.dir);
            logger.info("Start Crawling at: " + startUrl);
            logger.info(crawlerThread);
            if (crawlerThread == null || !crawlerThread.getState().toString().equals("RUNNABLE")){
                Crawler.resetState();
                crawlerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        crawler = new Crawler(startUrl, tmpDb, Integer.parseInt(size), Integer.parseInt(count));
                        crawler.start();
                        while (!crawler.isDone()){
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        crawler.shutdown();
                        tmpDb.close();
                    }
                }
                );
                crawlerThread.start();
            } else {
                halt(400, "Crawler is still running, should be shutdown first.");
            }
            return "Crawler Started";
        }
        else {
            halt(402, "None admin users are not allowed for start crawling.");
            return "";
        }

    }
}
