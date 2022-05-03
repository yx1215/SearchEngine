package edu.upenn.cis.cis455.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.tuple.Fields;

public class CrawlerTask {
	static Logger log = LogManager.getLogger(CrawlerTask.class);

	private static final String QUEUE_SPOUT = "QUEUE_SPOUT";
    private static final String DOC_FETCHER_BOLT = "DOC_FETCHER_BOLT";
    private static final String DOM_PARSER_BOLT = "DOM_PARSER_BOLT";
    private static final String LINK_EXTRACTOR_BOLT = "LINK_EXTRACTOR_BOLT";
    private static final String FILTER_URL_BOLT = "FILTER_URL_BOLT";
    private static final String PATH_MATCHER_BOLT = "PATH_MATCHER_BOLT";
    
    public static void build() throws Exception{
    	Config config = new Config();

        CrawlerQueueSpout spout = new CrawlerQueueSpout();
        DocumentFetcherBolt docFetcherBolt = new DocumentFetcherBolt(); 
        LinkExtractorBolt linkExtractorBolt = new LinkExtractorBolt();
        FilterUrlUpdateQueueBolt filterUrlUpdateQueueBolt = new FilterUrlUpdateQueueBolt();
//        DOMParserBolt domParserBolt = new DOMParserBolt();
//        PathMatcherBolt pathMatcherBolt = new PathMatcherBolt();
        //path matcher
        
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(QUEUE_SPOUT, spout, 1);
        builder.setBolt(DOC_FETCHER_BOLT, docFetcherBolt, 1).fieldsGrouping(QUEUE_SPOUT, new Fields("domain"));
        builder.setBolt(LINK_EXTRACTOR_BOLT, linkExtractorBolt, 1).shuffleGrouping(DOC_FETCHER_BOLT);
        builder.setBolt(FILTER_URL_BOLT, filterUrlUpdateQueueBolt, 1).shuffleGrouping(LINK_EXTRACTOR_BOLT);
//        builder.setBolt(DOM_PARSER_BOLT, domParserBolt, 4).shuffleGrouping(DOC_FETCHER_BOLT);
//        builder.setBolt(PATH_MATCHER_BOLT, pathMatcherBolt, 4).shuffleGrouping(DOM_PARSER_BOLT);

        
        LocalCluster cluster = new LocalCluster();
        Topology topo = builder.createTopology();

        ObjectMapper mapper = new ObjectMapper();
        
        cluster.submitTopology("test", config, 
        		builder.createTopology());
//        Thread.sleep(30000);
//        cluster.killTopology("test");
//        cluster.shutdown();
        
    }

}
