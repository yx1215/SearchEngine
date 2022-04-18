package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.cis455.storage.documentStorage.DatabaseDocument;
import edu.upenn.cis.cis455.storage.documentStorage.Storage;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import static edu.upenn.cis.cis455.crawler.utils.CrawlerHandler.*;

import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentFetcherBolt implements IRichBolt{

    static Logger logger = LogManager.getLogger(DocumentFetcherBolt.class);

    static AtomicBoolean isDelay = new AtomicBoolean(false);

    static AtomicInteger documentCount = new AtomicInteger(0);

    static int maxDocumentCount = 10;

    static boolean working = true;

    String executorId = UUID.randomUUID().toString();

    Fields schema = new Fields("url", "contentType", "content", "needAdd");

    private int size;

    private Storage db;

    private OutputCollector collector;

    private final HashMap<String, ArrayList<String>> disallow = new HashMap<>();

    private final HashMap<String, Integer> crawlDelay = new HashMap<>();

    private final HashMap<String, Long> checkPoints = new HashMap<>();

    public DocumentFetcherBolt(){
    }

    public static void resetState(){
        working = true;
        isDelay = new AtomicBoolean(false);
        documentCount = new AtomicInteger(0);
        maxDocumentCount = 10;
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(this.schema);
    }

    /**
     * Called when a bolt is about to be shut down
     */
    @Override
    public void cleanup() {
        logger.info("Executor " + getExecutorId() + " has handled host: " + this.disallow.keySet());
        this.disallow.clear();
        this.crawlDelay.clear();
        this.checkPoints.clear();
    }

    /**
     * Processes a tuple
     *
     * @param input
     */
    @Override
    public void execute(Tuple input) {
        if (working) {
            try {
                String host = input.getStringByField("host");
                String url = input.getStringByField("url");
                if (url != null) {
                    URLInfo urlInfo = new URLInfo(url);
                    if (!this.disallow.containsKey(host)) {
                        parseRobot(urlInfo, this.checkPoints, this.disallow, this.crawlDelay);
                        CrawlerSpout.addUrl(url);
                    } else {
                        if (!isDisallowed(urlInfo, this.disallow)) {
                            long lastCheck = this.checkPoints.get(urlInfo.getHostName()) == null ? new Date().getTime() : this.checkPoints.get(urlInfo.getHostName());
                            // TODO: change delay time when submitting.
                            if (new Date().getTime() - lastCheck <= this.crawlDelay.get(host) * 10) {
                                isDelay.set(true);
                                CrawlerSpout.addUrl(url);
                            } else {
                                isDelay.set(false);
                                HashMap<String, String> metaData = getUrlMetaData(url);
                                if (metaData.get("status").startsWith("3")){
                                    // if we need to redirect, add the location back to queue and exit
                                    String redirectLink = metaData.get("location");
                                    logger.info("Link " + url + " is redirecting to " + redirectLink);
                                    CrawlerSpout.addUrl(redirectLink);
                                    return;
                                }

                                // check content type
                                String contentType = metaData.get("content-type");
                                if (!contentType.endsWith("xml") && !contentType.endsWith("html")) {
                                    logger.info("Improper content type, skip path: " + url);
                                } else {
                                    DatabaseDocument curDocument = db.getDocument(url);
                                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                                    long lastCrawl = 0;
                                    long lastModified = 0;
                                    if (curDocument != null) {
                                        lastCrawl = curDocument.getLastModified().getTime();
                                        lastModified = metaData.get("last-modified") == null ? new Date().getTime() : formatter.parse(metaData.get("last-modified")).getTime();
                                    }
                                    if (Integer.parseInt(metaData.get("content-length")) > size * 1048576) {
                                        // if it exceeds maximum size.
                                        logger.info("Document for path " + url + " exceeds maximum size.");
                                    } else {
                                        String body;
                                        boolean needAdd = false;
                                        // download the content
                                        if (curDocument != null && lastModified <= lastCrawl) {
                                            // if not modified since last crawled.
                                            logger.info("Path " + url + " not modified since " + metaData.get("last-modified"));
                                            body = curDocument.getContent();
                                        } else {
                                            needAdd = true;
                                            body = getUrlContent(url);
                                            checkPoint(urlInfo.getHostName(), this.checkPoints);
                                            if (body == null) {
                                                throw new IOException();
                                            }
                                        }
                                        // if we didn't see the content in this crawl run, continue, otherwise, skip
                                        String tmp = db.checkContent(url, body);
                                        if (tmp == null) {
                                            if (needAdd) {
                                                if (documentCount.incrementAndGet() <= maxDocumentCount) {
                                                    db.addDocument(url, body, contentType);
                                                    logger.info(url + " added to database\n");
                                                } else {
                                                    working = false;
                                                }
                                            }
                                            this.collector.emit(new Values<Object>(url, contentType, body, needAdd));
                                        } else {
                                            logger.info("Duplicate content for url: " + url + " and " + tmp);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Called when this task is initialized
     *
     * @param stormConf
     * @param context
     * @param collector
     */
    @Override
    public void prepare(Map<String, Object> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.db = (Storage) stormConf.get("db");
        this.size = (int) stormConf.get("size");
        maxDocumentCount = (int) stormConf.get("maxDocumentCount");
    }

    /**
     * Called during topology creation: sets the output router
     *
     * @param router
     */
    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    /**
     * Get the list of fields in the stream tuple
     *
     * @return
     */
    @Override
    public Fields getSchema() {
        return this.schema;
    }
}
