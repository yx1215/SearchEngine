package edu.upenn.cis.cis455.Indexer;

import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.documentStorage.Storage;
import edu.upenn.cis.cis455.storage.indexStorage.Index;
import edu.upenn.cis.cis455.storage.indexStorage.IndexStorage;
import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Indexer {
    final static Logger logger = LogManager.getLogger(Indexer.class);

    private static final String INDEXER_SPOUT = "INDEXER_SPOUT";
    private static final String INDEXER_BOLT = "INDEXER_BOLT";

    public static void main(String[] args) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        LocalCluster cluster = new LocalCluster();

        IndexStorage indexStorage = StorageFactory.getIndexDatabase("./indexDatabase");
        Storage documentStorage = (Storage) StorageFactory.getDocumentDatabase("./database");
        Config config = new Config();
        config.put("DocumentDb", documentStorage);
        config.put("IndexDb", indexStorage);

        IndexerSpout indexerSpout = new IndexerSpout();
        IndexerBolt indexerBolt = new IndexerBolt();
        TopologyBuilder builder = new TopologyBuilder();


        builder.setSpout(INDEXER_SPOUT, indexerSpout, 1);
        builder.setBolt(INDEXER_BOLT, indexerBolt, 4).shuffleGrouping(INDEXER_SPOUT);

        cluster.submitTopology("test", config,
                builder.createTopology());

        while (IndexerSpout.docId.get() < documentStorage.getCorpusSize() || cluster.getActiveCount() != 0){
            System.out.println(cluster.getActiveCount());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cluster.killTopology("test");
        while (!cluster.isTerminated()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cluster.shutdown();
        documentStorage.close();
        indexStorage.close();
    }
}
