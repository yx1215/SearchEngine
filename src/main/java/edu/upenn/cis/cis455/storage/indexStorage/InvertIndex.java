package edu.upenn.cis.cis455.storage.indexStorage;

import java.io.Serializable;
import java.util.HashMap;

public class InvertIndex implements Serializable {

    public int wordId;
    // docId to corresponding indexId
    public HashMap<Integer, Integer> invertIndices = new HashMap<>();

    public InvertIndex(int wordId){
        this.wordId = wordId;
    }

    public void addInvertIndex(int docId, int indexId){
        this.invertIndices.put(docId, indexId);
    }

    public int nDoc(){
        return this.invertIndices.size();
    }

    public int getWordId() {
        return wordId;
    }

    public HashMap<Integer, Integer> getInvertIndices() {
        return invertIndices;
    }
}
