package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.utils.URLInfo;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.BlockingArrayQueue;

import java.util.ArrayList;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CrawlerSpout implements IRichSpout{
    static Logger logger = LogManager.getLogger(CrawlerSpout.class);

    String executorId = UUID.randomUUID().toString();

    SpoutOutputCollector collector;

    public static LinkedBlockingQueue<String> sharedQueue = new LinkedBlockingQueue<>();

    private Fields schema = new Fields("host", "url");

    public CrawlerSpout(){
        logger.info("Starting spout");
    }

    public static void addUrl(String url){
        try {
            sharedQueue.put(url);
//            System.out.println(sharedQueue.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isEmpty(){
        return sharedQueue.isEmpty();
    }

    public static void resetQueue(){
        sharedQueue.clear();
        sharedQueue = new LinkedBlockingQueue<>();
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
        String nextUrl = sharedQueue.poll();
//        System.out.println(nextUrl);
        if (nextUrl != null){
//            System.out.println("next url is: " + nextUrl);
            URLInfo urlInfo = new URLInfo(nextUrl);
            String formattedUrl = urlInfo.toString();
            String host = urlInfo.getHostName();
            if (host == null){
                logger.warn("Invalid url: " + nextUrl);
            } else {
                this.collector.emit(new Values<Object>(host, formattedUrl));
            }
        }

//        Thread.yield();

    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }
}
