package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.UUID;


public class LinkExtractorBolt implements IRichBolt {
    static Logger logger = LogManager.getLogger(LinkExtractorBolt.class);

    String executorId = UUID.randomUUID().toString();

    private final Fields fields = new Fields();

    private Storage db;

    public LinkExtractorBolt(){
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(fields);
    }

    /**
     * Called when a bolt is about to be shut down
     */
    @Override
    public void cleanup() {

    }

    /**
     * Processes a tuple
     *
     * @param input
     */
    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField("url");
        String contentType = input.getStringByField("contentType");
        String body = input.getStringByField("content");

        if (!contentType.contains("xml")) {
            // if not xml type, we extract links from it.
            logger.info("Extracting links for " + url);
            Document doc;
            doc = Jsoup.parse(body, url);
            Elements links = doc.select("a");
            for (Element link: links){
                String nextLink = link.attr("abs:href");
                CrawlerSpout.addUrl(nextLink);
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
        this.db = (Storage) stormConf.get("db");
    }

    /**
     * Called during topology creation: sets the output router
     *
     * @param router
     */
    @Override
    public void setRouter(IStreamRouter router) {

    }

    /**
     * Get the list of fields in the stream tuple
     *
     * @return
     */
    @Override
    public Fields getSchema() {
        return this.fields;
    }
}
