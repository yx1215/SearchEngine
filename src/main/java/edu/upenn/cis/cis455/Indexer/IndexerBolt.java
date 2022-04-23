package edu.upenn.cis.cis455.Indexer;

import edu.upenn.cis.IndexerHelper;
import edu.upenn.cis.cis455.storage.documentStorage.Storage;
import edu.upenn.cis.cis455.storage.indexStorage.IndexStorage;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IndexerBolt implements IRichBolt {

    static Logger logger = LogManager.getLogger(IndexerBolt.class);

    String executorId = UUID.randomUUID().toString();

    private final Fields schema = new Fields("url", "event", "type");

    private OutputCollector collector;

    private Storage documentDb;
    private IndexStorage indexDb;

    public IndexerBolt() {

    }

    @Override
    public String getExecutorId() {
        return this.executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

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
        String content = input.getStringByField("content");
        int docId = this.documentDb.getDocId(url);
        if (docId != -1){
            logger.info("Indexing url: " + url);
            ArrayList<String> words = IndexerHelper.lemmatize(Jsoup.parse(content).text());
            logger.info("Finish lemmatizing url: " + url);
            int pos = 0;
            HashMap<String, ArrayList<Integer>> wordPos = new HashMap<>();
            for (String word: words){
                if (!wordPos.containsKey(word)){
                    wordPos.put(word, new ArrayList<>());
                }
                ArrayList<Integer> tmpPos = wordPos.get(word);
                tmpPos.add(pos);
                pos ++;
            }
            indexDb.addIndexHits(docId, wordPos);
            this.indexDb.indexDatabase.sync();
            logger.info("Finish indexing url: " + url);
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
        this.indexDb = (IndexStorage) stormConf.get("IndexDb");
        this.documentDb = (Storage) stormConf.get("DocumentDb");
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
        return schema;
    }
}
