package edu.upenn.cis.cis455.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;

import edu.upenn.cis.stormlite.*;
import edu.upenn.cis.stormlite.tuple.Fields;
import org.apache.logging.log4j.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import static edu.upenn.cis.cis455.crawler.utils.CrawlerHandler.*;

public class Crawler implements CrawlMaster {
    // TODO: you'll need to flesh all of this out. You'll need to build a thread pool of CrawlerWorkers etc.
    final static Logger logger = LogManager.getLogger(Crawler.class);

    static final int NUM_WORKERS = 10;

    private static final String CRAWLER_SPOUT = "CRAWLER_SPOUT";
    private static final String DOCUMENT_FETCHER_BOLT = "DOCUMENT_FETCHER_BOLT";
    private static final String LINK_EXTRACTOR_BOLT = "LINK_EXTRACTOR_BOLT";
    private static final String DOM_PARSE_BOLT = "DOM_PARSE_BOLT";
    private static final String PATH_MATCHER_BOLT = "PATH_MATCHER_BOLT";

    String startUrl;

//    private final Object lock = new Object();

    private boolean forceShutdown = false;
    private boolean working = true;
    private boolean isDelay = false;

    public int documentCounts = 0;
    public int size;

    private final int maxDocumentCount;
    private final Storage db;

    private LocalCluster cluster = new LocalCluster();

    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        this.maxDocumentCount = count;
        this.startUrl = startUrl;
        this.db = (Storage) db;
        this.size = size;
    }

    /**
     * Main thread
     */
    public void start() {
        CrawlerSpout.addUrl(this.startUrl);

        Config config = new Config();
        config.put("db", this.db);
        config.put("size", this.size);
        config.put("maxDocumentCount", this.maxDocumentCount);

        CrawlerSpout spout = new CrawlerSpout();

        DocumentFetcherBolt documentFetcherBolt = new DocumentFetcherBolt();
        LinkExtractorBolt linkExtractorBolt = new LinkExtractorBolt();
        DOMParseBolt domParseBolt = new DOMParseBolt();
        PathMatcherBolt pathMatcherBolt = new PathMatcherBolt();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(CRAWLER_SPOUT, spout, 1);

        builder.setBolt(DOCUMENT_FETCHER_BOLT, documentFetcherBolt, 4).fieldsGrouping(CRAWLER_SPOUT, new Fields("host"));

        builder.setBolt(LINK_EXTRACTOR_BOLT, linkExtractorBolt, 4).shuffleGrouping(DOCUMENT_FETCHER_BOLT);

        builder.setBolt(DOM_PARSE_BOLT, domParseBolt, 1).shuffleGrouping(DOCUMENT_FETCHER_BOLT);

        builder.setBolt(PATH_MATCHER_BOLT, pathMatcherBolt, 1).fieldsGrouping(DOM_PARSE_BOLT, new Fields("url"));

        Topology topology = builder.createTopology();

        ObjectMapper mapper = new ObjectMapper();
        try {
            String str = mapper.writeValueAsString(topology);

//            System.out.println("The StormLite topology is:\n" + str);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.cluster.submitTopology("test", config,
                                    builder.createTopology());
    }
    /**
     * We've indexed another document
     */
    @Override
    public void incCount() {
        this.documentCounts += 1;
    }

    /**
     * Workers can poll this to see if they should exit, ie the crawl is done
     */
    @Override
    public boolean isDone() {
//        logger.info("Current active threads: " + this.cluster.getActiveCount());
//        logger.info("Crawled Document Count: " + LinkExtractorBolt.documentCount.get());
        return (this.forceShutdown) || (!DocumentFetcherBolt.working || (CrawlerSpout.isEmpty() && cluster.getActiveCount() == 0 && !DocumentFetcherBolt.isDelay.get()));
    }

    public void forceShutdown(){
        this.forceShutdown = true;
    }

    public void shutdown(){
        cluster.killTopology("test");
        while (!cluster.isTerminated()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cluster.shutdown();
        logger.info("Done crawling! Current database contain "+db.getCorpusSize()+" pages.");
    }

    public static void resetState(){
        CrawlerSpout.resetQueue();
        DocumentFetcherBolt.resetState();
//        this.cluster = new LocalCluster();
    }

    /**
     * Workers should notify when they are processing an URL
     */
    @Override
    public void setWorking(boolean working) {
        this.working = working;
    }

    /**
     * Workers should call this when they exit, so the master knows when it can shut
     * down
     */
    @Override
    public void notifyThreadExited() {
    }

    /**
     * Main program: init database, start crawler, wait for it to notify that it is
     * done, then close.
     */
    public static void main(String args[]) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        if (args.length < 3 || args.length > 5) {
            logger.info("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }

        logger.info("Crawler starting");
        String startUrl = args[0];
//        String startUrl = "http://localhost:45555/index.html";
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;

        StorageInterface db = StorageFactory.getDatabaseInstance(envPath);
        logger.info("Current database contains " + db.getCorpusSize() + " pages.");
//        System.out.println(getUrlContent(startUrl));
        Crawler crawler = new Crawler(startUrl, db, size, count);
        logger.info("Starting crawl of " + count + " documents, starting at " + startUrl);

        crawler.start();
        while (!crawler.isDone()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // final shutdown
        crawler.shutdown();
        db.close();

    }

}
