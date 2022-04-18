package edu.upenn.cis.cis455.Indexer;

import edu.upenn.cis.cis455.crawler.CrawlerSpout;
import edu.upenn.cis.cis455.storage.documentStorage.DatabaseDocument;
import edu.upenn.cis.cis455.storage.documentStorage.Storage;
import edu.upenn.cis.cis455.storage.indexStorage.Index;
import edu.upenn.cis.cis455.storage.indexStorage.IndexStorage;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexerSpout implements IRichSpout {

    static Logger logger = LogManager.getLogger(IndexerSpout.class);

    String executorId = UUID.randomUUID().toString();

    SpoutOutputCollector collector;

    Storage documentDb;
    static AtomicInteger docId = new AtomicInteger(0);

    private Fields schema = new Fields("url", "content");

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }

    /**
     * Called when a task for this component is initialized within a
     * worker on the cluster. It provides the spout with the environment
     * in which the spout executes.
     *
     * @param config    The Storm configuration for this spout. This is
     *                  the configuration provided to the topology merged in
     *                  with cluster configuration on this machine.
     * @param topo
     * @param collector The collector is used to emit tuples from
     *                  this spout. Tuples can be emitted at any time, including
     *                  the open and close methods. The collector is thread-safe
     *                  and should be saved as an instance variable of this spout
     */
    @Override
    public void open(Map<String, Object> config, TopologyContext topo, SpoutOutputCollector collector) {
        this.documentDb = (Storage) config.get("DocumentDb");
        this.collector = collector;
    }

    /**
     * Called when an ISpout is going to be shutdown.
     * There is no guarantee that close will be called, because the
     * supervisor kill -9’s worker processes on the cluster.
     */
    @Override
    public void close() {

    }

    /**
     * When this method is called, Storm is requesting that the Spout emit
     * tuples to the output collector. This method should be non-blocking,
     * so if the Spout has no tuples to emit, this method should return.
     */
    @Override
    public void nextTuple() {
        if (docId.get() < documentDb.getCorpusSize()){
            int id = docId.getAndIncrement();
            DatabaseDocument doc = documentDb.getDocById(id);
            this.collector.emit(new Values<>(doc.getUrl(), doc.getContent()));
        }
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }
}
