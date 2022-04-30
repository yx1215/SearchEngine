package edu.upenn.cis;

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
        String query = "Lebron James";
        List<String> urls = IndexerHelper.processQuery(query, 10);
        System.out.println("Search results for: " + query);
        for (String url: urls){
            System.out.println(url);
        }
        System.out.println(new Date().getTime() - start);
//        IndexerHelper.createTable(IndexerHelper.ForwardIndex.class);
    }
}