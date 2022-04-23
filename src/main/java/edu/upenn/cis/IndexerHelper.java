package edu.upenn.cis;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.upenn.cis.cis455.storage.indexStorage.IndexStorage;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import static java.util.Map.Entry.*;
import static java.util.stream.Collectors.toMap;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class IndexerHelper {
    public static String accessKey = "AKIAX5K2P2746CPKER5X";
    public static String secretKey = "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";
    public static String crawlBucketName = "crawleddocuments";

    @DynamoDBTable(tableName = "RowCounts")
    public static class RowCounts{

        private String tableClassName;
        private int rowCount;

        @DynamoDBHashKey(attributeName = "tableClassName")
        public String getTableClassName() {return tableClassName;}

        public void setTableClassName(String tableClassName) {this.tableClassName = tableClassName;}

        @DynamoDBAttribute(attributeName = "rowCount")
        public int getRowCount() {
            return rowCount;
        }

        public void setRowCount(int rowCount) {this.rowCount = rowCount;}

        public RowCounts(){}

    }
    @DynamoDBTable(tableName = "ForwardIndex")
    public static class ForwardIndex{

        private String docId;
        // word to numOccur
        private HashMap<String, Integer> forwardIndex = new HashMap<>();
        private int squareNorm;

        @DynamoDBAttribute(attributeName = "forwardIndex")
        public HashMap<String, Integer> getForwardIndex() { return forwardIndex;}

        public void setForwardIndex(HashMap<String, Integer> forwardIndex) {this.forwardIndex = forwardIndex;}

        @DynamoDBAttribute(attributeName = "squareNorm")
        public int getSquareNorm() {return squareNorm;}

        public void setSquareNorm(int squareNorm) {this.squareNorm = squareNorm;}

        @DynamoDBHashKey(attributeName = "docId")
        public String getDocId() {return docId;}

        public void setDocId(String docId) {this.docId = docId;}

        public ForwardIndex(){}

        public void addForwardIndex(String word, int numOccur){
            this.squareNorm += Math.pow(numOccur, 2);
            this.forwardIndex.put(word, numOccur);
        }

    }

    @DynamoDBTable(tableName = "InvertIndex")
    public static class InvertIndex{
        private String word;
        private HashMap<String, Integer> invertIndex = new HashMap<>();

        @DynamoDBAttribute(attributeName = "invertIndex")
        public HashMap<String, Integer> getInvertIndex() {return invertIndex;}

        public void setInvertIndex(HashMap<String, Integer> invertIndex) {this.invertIndex = invertIndex;}

        @DynamoDBHashKey(attributeName = "word")
        public String getWord() {return word;}

        public void setWord(String word) {this.word = word;}

        public int nDoc(){return this.invertIndex.size();}

        public InvertIndex(){}

        public void addInvertIndex(String docId, int numOccur){
            this.invertIndex.put(docId, numOccur);
        }

    }

    public static AmazonDynamoDB getDynamoDB(){
        AWSCredentials credentials = new BasicAWSCredentials(
                accessKey,
                secretKey
        );

        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public static AmazonS3 getAmazonS3Client(){
        AWSCredentials credentials = new BasicAWSCredentials(
                accessKey,
                secretKey
        );

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public static ArrayList<String> lemmatize(String content){

        ArrayList<String> words = new ArrayList<>();
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = pipeline.processToCoreDocument(content);

        // display tokens
        for (CoreLabel tok : document.tokens()) {
//            System.out.printf("%s\t%s%n", tok.word(), tok.lemma());
            words.add(tok.lemma().toLowerCase(Locale.ROOT));
        }
        return words;
    }

    public static List<Integer> processQuery(String query, IndexStorage db, int n){
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = pipeline.processToCoreDocument(query);
        ArrayRealVector queryWeights = new ArrayRealVector();
        for (CoreLabel tok : document.tokens()) {
            queryWeights = (ArrayRealVector) queryWeights.append(db.getInverseDocFreq(tok.lemma()));
        }
        HashMap<Integer, Double> docScore = new HashMap<>();
        HashSet<Integer> allDocId = new HashSet<>();
        for (CoreLabel tok : document.tokens()) {
            allDocId.addAll(db.getAllDocId(tok.lemma()));
        }
        System.out.println(allDocId.size());
        for (int tmp: allDocId){
            ArrayRealVector docWeights = new ArrayRealVector();
            for (CoreLabel tok : document.tokens()) {
                docWeights = (ArrayRealVector) docWeights.append(db.getTermFreq(tmp, tok.lemma()));
            }
            double score = docWeights.dotProduct(queryWeights);
            if (score > 0){
                docScore.put(tmp, score);
            }
        }
        docScore = docScore.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        System.out.println(docScore);
        System.out.println(new ArrayList<>(docScore.keySet()).subList(0, n));
//        System.out.println(docScore);
        return new ArrayList<>(docScore.keySet()).subList(0, n);
    }

    public static List<String> processQuery2(String query, int topN){

        AmazonDynamoDB dynamoDB = getDynamoDB();
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = pipeline.processToCoreDocument(query);


        ArrayList<String> words = new ArrayList<>();
        for (CoreLabel tok : document.tokens()) {
            words.add(tok.lemma().toLowerCase(Locale.ROOT));
        }
        Set<String> allDocIds = getAllDocId(words, dynamoDB);
        HashMap<String, Double> docScore = new HashMap<>();
        ArrayRealVector queryWeights = getQueryWeights(words, dynamoDB);

        for (String docId: allDocIds){
            ArrayRealVector docWeights = getDocWeights(docId, words, dynamoDB);
            double score = docWeights.dotProduct(queryWeights);
            if (score > 0){
                docScore.put(docId, score);
            }
        }
        docScore = docScore.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

//        System.out.println(docScore);
        List<String> topNDocId = new ArrayList<>(docScore.keySet()).subList(0, topN);
        System.out.println(topNDocId);
        return getUrls(topNDocId, dynamoDB);
    }

    public static ArrayRealVector getDocWeights(String docId, List<String> words, AmazonDynamoDB dynamoDB){
        ArrayRealVector docWeights = new ArrayRealVector();
        for (String word: words) {
            docWeights = (ArrayRealVector) docWeights.append(getTermFreq(docId, word, dynamoDB));
        }
        return docWeights;
    }

    public static ArrayRealVector getQueryWeights(List<String> words, AmazonDynamoDB dynamoDB){
        ArrayRealVector queryWeights = new ArrayRealVector();
        for (String word: words){
            queryWeights = (ArrayRealVector) queryWeights.append(getInverseDocFreq(word, dynamoDB));
        }
        return queryWeights;
    }

    public static Double getInverseDocFreq(String word, AmazonDynamoDB dynamoDB){
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);

        InvertIndex invertIndex = mapper.load(InvertIndex.class, word);
        RowCounts rowCounts = mapper.load(RowCounts.class, ForwardIndex.class.getName());
        int totalDoc = rowCounts.getRowCount();
        if (invertIndex == null){
            System.out.println("word: " + word + "is not indexed.");
            return 0.0;
        }
        return  Math.log10((double) totalDoc / invertIndex.nDoc());
    }

    public static double getTermFreq(String docId, String word, AmazonDynamoDB dynamoDB){
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);

        ForwardIndex forwardIndex = mapper.load(ForwardIndex.class, docId);
        double norm = Math.pow(forwardIndex.getSquareNorm(), 0.5);
        if (forwardIndex.getForwardIndex().get(word) == null){
            return 0.0;
        }
        return forwardIndex.getForwardIndex().get(word) / norm;
    }

    public static Set<String> getAllDocId(List<String> words, AmazonDynamoDB dynamoDB){
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);

        Set<String> docIds = new HashSet<>();
        for (String word: words){
            InvertIndex invertIndex = mapper.load(InvertIndex.class, word);
            if (invertIndex != null){
                docIds.addAll(invertIndex.getInvertIndex().keySet());
            }
        }
        return docIds;
    }

    public static List<String> getUrls(List<String> docIds, AmazonDynamoDB dynamoDB){
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
        List<String> urls = new ArrayList<>();
        for (String docId: docIds){
            urls.add(mapper.load(DocidToUrlItem.class, docId).getUrl());
        }
        return urls;
    }

    public static String getUrlContent(String url, boolean download) throws IOException {
        if (download){
            URL newUrl = new URL(url);
            HttpURLConnection connection;
            if (url.startsWith("https://")){
                connection = (HttpsURLConnection) newUrl.openConnection();
            }
            else {
                connection = (HttpURLConnection) newUrl.openConnection();
            }
            return new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            AmazonDynamoDB dynamoDB = getDynamoDB();
            AmazonS3 s3client = getAmazonS3Client();

            DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
            String docId = mapper.load(UrlToDocId.class, url).getDocid();
            String fileId = mapper.load(FileToDoc.class, docId).getFileId();

            S3Object s3object = s3client.getObject(crawlBucketName, fileId);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            String fileContent = new String(inputStream.readAllBytes());

            int startIndex = fileContent.indexOf(docId) + docId.length();
            int endIndex = fileContent.indexOf("***DOCID", startIndex);

            return fileContent.substring(startIndex, endIndex);
        }
    }

    public static void createTable(Class c){
        AmazonDynamoDB dynamoDB = getDynamoDB();
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
        CreateTableRequest req = mapper.generateCreateTableRequest(c);
        req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        dynamoDB.createTable(req);
    }
}