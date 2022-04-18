package edu.upenn.cis.cis455.Indexer;


import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.upenn.cis.cis455.storage.indexStorage.IndexStorage;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import static java.util.Map.Entry.*;
import static java.util.stream.Collectors.toMap;


import java.util.*;


public class IndexerHandler {
    
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
            words.add(tok.lemma());
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
            System.out.printf("%s\t%s%n", tok.word(), tok.lemma());
            queryWeights = (ArrayRealVector) queryWeights.append(db.getInverseDocFreq(tok.lemma()));
        }
        HashMap<Integer, Double> docScore = new HashMap<>();
        for (Object tmp: db.getAllDocId()){
            ArrayRealVector docWeights = new ArrayRealVector();
            int docId = (int) tmp;
            for (CoreLabel tok : document.tokens()) {
                docWeights = (ArrayRealVector) docWeights.append(db.getTermFreq(docId, tok.lemma()));
            }
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
        System.out.println(docScore);
        System.out.println(new ArrayList<>(docScore.keySet()).subList(0, n));
//        System.out.println(docScore);
        return new ArrayList<>(docScore.keySet()).subList(0, n);
    }

}
