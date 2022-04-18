package edu.upenn.cis.cis455.storage.indexStorage;

import edu.upenn.cis.cis455.storage.documentStorage.Storage;
import edu.upenn.cis.cis455.storage.documentStorage.StorageDatabase;
import edu.upenn.cis.cis455.storage.documentStorage.StorageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexStorage {
    final static Logger logger = LogManager.getLogger(IndexStorage.class);

    public final IndexDatabase indexDatabase;
    public final IndexView indexView;

    public String dir;

    public AtomicInteger wordId;
    public AtomicInteger indexId;

    public IndexStorage(String dir){
        this.indexDatabase = new IndexDatabase(dir);
        this.indexView = new IndexView(this.indexDatabase);
        this.dir = dir;
        this.wordId = new AtomicInteger(this.indexView.getWord2wordIdMap().size());
        this.indexId = new AtomicInteger(this.indexView.getIndexId2IndexMap().size());
    }

    public void close(){
        indexDatabase.close();
    }

    public int getCorpusSize() {
        return this.indexView.getForwardIndexMap().size();
    }

    public void addIndexHits(int docId, HashMap<String, ArrayList<Integer>> wordPoses){
        Map word2wordId = this.indexView.getWord2wordIdMap();
        Map indexString2IndexId = this.indexView.getIndexString2indexIdMap();
        Map indexId2index = this.indexView.getIndexId2IndexMap();
        Map forwardIndexMap = this.indexView.getForwardIndexMap();
        Map invertIndexMap = this.indexView.getInvertIndexMap();

        if (!forwardIndexMap.containsKey(docId)){
            forwardIndexMap.put(docId, new ForwardIndex(docId));
        }

        ForwardIndex forwardIndex = (ForwardIndex) forwardIndexMap.get(docId);

        for (String word: wordPoses.keySet()){
            int wordId, indexId;
            if (word2wordId.containsKey(word)){
                wordId = (int) word2wordId.get(word);
            }
            else {
                wordId = this.wordId.getAndIncrement();
                word2wordId.put(word, wordId);
            }
            String indexString = docId + "@" + wordId;

            ArrayList<Integer> allPos = wordPoses.get(word);

            if (indexString2IndexId.containsKey(indexString)){
                indexId = (int) indexString2IndexId.get(indexString);
            }
            else {
                indexId = this.indexId.getAndIncrement();
                indexString2IndexId.put(indexString, indexId);
                indexId2index.put(indexId, new Index(wordId, docId));
            }

            Index index = (Index) indexId2index.get(indexId);
            index.addHits(word, allPos);
            indexId2index.put(indexId, index);

            if (!invertIndexMap.containsKey(wordId)){
                invertIndexMap.put(wordId, new InvertIndex(wordId));
            }
            InvertIndex invertIndex = (InvertIndex) invertIndexMap.get(wordId);
            invertIndex.addInvertIndex(docId, indexId);
            invertIndexMap.put(wordId, invertIndex);

            forwardIndex.addForwardIndex(wordId, indexId, index.getNumHits());
        }
        forwardIndexMap.put(docId, forwardIndex);

    }

    public ForwardIndex getForwardIndex(int docId){
        Map forwardIndexMap = this.indexView.getForwardIndexMap();
        return (ForwardIndex) forwardIndexMap.get(docId);
    }

    public InvertIndex getInvertIndex(String word){
        Map word2wordId = this.indexView.getWord2wordIdMap();
        Map invertIndexMap = this.indexView.getInvertIndexMap();
        if (!word2wordId.containsKey(word)){
            return null;
        }

        int wordId = (int) word2wordId.get(word);
        return (InvertIndex) invertIndexMap.get(wordId);
    }

    public Index getIndex(int indexId){
        Map indexId2index = this.indexView.getIndexId2IndexMap();
        return (Index) indexId2index.get(indexId);
    }

    public double getTermFreq(int docId, String word){
        ForwardIndex forwardIndex = this.getForwardIndex(docId);
        if (forwardIndex == null){
            logger.warn("Cannot find docId: " + docId);
            return 0;
        }
        Map word2wordId = this.indexView.getWord2wordIdMap();
        if (!word2wordId.containsKey(word)){
            return 0;
        }

        int wordId = (int) word2wordId.get(word);


//        int curWordCount = 0;
//        for (int tmpId: forwardIndex.getForwardIndices().keySet()){
//            Index tmpIndex = this.getIndex(forwardIndex.getForwardIndices().get(tmpId));
//            if (tmpId == wordId){
//                curWordCount = tmpIndex.getNumHits();
//            }
//            norm += Math.pow(tmpIndex.getNumHits(), 2);
//        }
        String indexString = docId + "@" + wordId;
        Map indexString2indexId = this.indexView.getIndexString2indexIdMap();
        if (!indexString2indexId.containsKey(indexString)){
            return 0;
        }
        int indexId = (int) this.indexView.getIndexString2indexIdMap().get(indexString);
        Index index = (Index) this.indexView.getIndexId2IndexMap().get(indexId);
        double norm = Math.pow(forwardIndex.getSquareNorm(), 0.5);

        return index.getNumHits() / norm;
    }

    public double getInverseDocFreq(String word){
        Map word2wordId = this.indexView.getWord2wordIdMap();
        if (!word2wordId.containsKey(word)){
            return 0;
        }
        InvertIndex invertIndex = this.getInvertIndex(word);
        System.out.println("Corpus Size " + this.getCorpusSize());
        System.out.println("Doc appearance of " + word + ": " + invertIndex.nDoc());
        return Math.log10((double) this.getCorpusSize() / invertIndex.nDoc());
    }

    public Set getAllDocId(){
        return this.indexView.getForwardIndexMap().keySet();
    }


}
