package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.storage.Channel;
import edu.upenn.cis.cis455.storage.Storage;
import edu.upenn.cis.cis455.xpathengine.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.XPathEngine;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineImp;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PathMatcherBolt implements IRichBolt {

    String executorId = UUID.randomUUID().toString();

    static Logger logger = LogManager.getLogger(PathMatcherBolt.class);

    private OutputCollector collector;

    private Storage db;

    private final HashMap<String, XPathEngine> xPathEngineImpHashMap = new HashMap<>();
    public PathMatcherBolt(){

    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    /**
     * Called when a bolt is about to be shut down
     */
    @Override
    public void cleanup() {
        logger.info("Executor " + getExecutorId() + "evaluated urls: "+ this.xPathEngineImpHashMap.keySet());
        this.xPathEngineImpHashMap.clear();
    }

    /**
     * Processes a tuple
     *
     * @param input
     */
    @Override
    public void execute(Tuple input) {

        ArrayList<String> channelNames = new ArrayList<>();
        ArrayList<String> expressions = new ArrayList<>();
        for (Object e: this.db.getAllChannels()){
            String channelName = e.toString();
            channelNames.add(channelName);
            Channel channel = this.db.getChannel(channelName);
            expressions.add(channel.getXPath());
        }

        String url = input.getStringByField("url");
        String type = input.getStringByField("type");

        OccurrenceEvent event = (OccurrenceEvent) input.getObjectByField("event");
        if (event.getLevel() == 0){
            if (event.getValue().equals("START")){
                // if we encounter a new document, initialize a new engine and set channels xpath
                XPathEngine tmpEngine = XPathEngineFactory.getXPathEngine();
                tmpEngine.setXPaths(expressions.toArray(new String[0]));
                this.xPathEngineImpHashMap.put(url, tmpEngine);
            }
            else if (event.getValue().equals("END")){
                // if matched, save to database
                XPathEngine curEngine = this.xPathEngineImpHashMap.get(url);
//                if (this.db.getDocumentObject(url) != null){
                System.out.println("Match results for url " + url + " : " + Arrays.toString(curEngine.getMatchResult()));
//                }

                boolean[] matchResults = curEngine.getMatchResult();
                for (int i=0;i<matchResults.length; i++){
                    if (matchResults[i] && this.db.getDocumentObject(url) != null){
                        String channelName = channelNames.get(i);
                        Channel tmpChannel = this.db.getChannel(channelName);
                        tmpChannel.addUrl(url);
                        this.db.updateChannel(channelName, url);
                    }
                }
            }
        }
        else {
            // evaluate events
            XPathEngine curEngine = this.xPathEngineImpHashMap.get(url);
            boolean[] matchResults = curEngine.evaluateEvent(event, type);
            for (int i=0;i<matchResults.length; i++){
                if (matchResults[i] && this.db.getDocumentObject(url) != null){
                    String channelName = channelNames.get(i);
                    Channel tmpChannel = this.db.getChannel(channelName);
                    tmpChannel.addUrl(url);
                    this.db.updateChannel(channelName, url);
                }
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
        return null;
    }
}
