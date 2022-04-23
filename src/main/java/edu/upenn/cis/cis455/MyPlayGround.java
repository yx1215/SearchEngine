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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MyPlayGround {

    public static void main(String[] args) throws IOException {
//        String query = "Aston Villa";
//        List<String> urls = IndexerHelper.processQuery2(query, 10);
//        System.out.println("Search results for: " + query);
//        for (String url: urls){
//            System.out.println(url);
//        }
        System.out.println(IndexerHelper.getUrlContent("https://www.bbc.co.uk/sport/football/teams/aston-villa", false));
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