package edu.upenn.cis.cis455;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import edu.upenn.cis.IndexerHelper;
import edu.upenn.cis.cis455.storage.indexStorage.Index;
import org.jsoup.Jsoup;

import javax.json.Json;
import java.io.IOException;
import java.util.*;

public class MyPlayGround {

    public static void main(String[] args) throws IOException {
        long start = new Date().getTime();
        String query = "golden state warriors";
        List<String> urls = IndexerHelper.processQuery(query, 10);
        System.out.println("Search results for: " + query);
        for (String url: urls){
            System.out.println(url);
        }
        System.out.println(new Date().getTime() - start);
//        urls = IndexerHelper.processQuery2(query, 10);
//        System.out.println("Search results for: " + query);
//        for (String url: urls){
//            System.out.println(url);
//        }
//        HashMap<String, List<Integer>> hitLists = new HashMap<>();
//        String htmlText = Jsoup.parse(IndexerHelper.getUrlContent("https://news.yahoo.com/warriors-bringing-stephen-curry-off-222139475.html", true)).text();
//        int pos = 0;
//        for (String word: IndexerHelper.lemmatize(htmlText.replaceAll("\\p{Punct}", " "))){
//            List<Integer> curPos = hitLists.getOrDefault(word, new ArrayList<>());
//            curPos.add(pos);
//            hitLists.put(word, curPos);
//            pos ++;
//        }
//        System.out.println(hitLists.get("stephen"));
//        System.out.println(hitLists.get("curry"));
//        IndexerHelper.ForwardIndex forwardIndex = new DynamoDBMapper(IndexerHelper.getDynamoDB()).load(IndexerHelper.ForwardIndex.class, "5927c926-7f22-4468-8276-1e3e6cf95f8d");
//        System.out.println(forwardIndex.getHitLists().get("stephen"));
//        System.out.println(forwardIndex.getHitLists().get("curry"));
//        System.out.println(IndexerHelper.getUrlContent("https://www.bbc.co.uk/sport/football/teams/aston-villa", false));
//        IndexerHelper.createTable(IndexerHelper.ForwardIndex.class);
    }
}


//        Storage db = (Storage) StorageFactory.getDocumentDatabase("./database");
//        System.out.println(db.getDocId("https://en.wikipedia.org:443/wiki/Black_Sea_Fleet"));
//        IndexStorage indexStorage = StorageFactory.getIndexDatabase("./indexDatabase");
//        long time1 = new Date().getTime();
//        List<Integer> docIds = IndexerHandler.processQuery("Black Sea Fleet", indexStorage, 5);
//        long time2 = new Date().getTime();
//        System.out.println((double) (time2 - time1) / 1000);
//        for (int tmp: docIds){
//            System.out.println(db.getUrlById(tmp));
//        }