package edu.upenn.cis.cis455.storage.indexStorage;

import java.io.Serializable;
import java.util.HashMap;

public class ForwardIndex implements Serializable {

    private final int docId;
    private final HashMap<Integer, Integer> forwardIndices = new HashMap<>();
    private int nWords = 0;
    private int squareNorm;

    public ForwardIndex(int docId){
        this.docId = docId;
    }

    public void addForwardIndex(int wordId, int indexId, int nWords){
        this.nWords += nWords;
        this.squareNorm += Math.pow(nWords, 2);
        this.forwardIndices.put(wordId, indexId);
    }

    public int nWords(){
        return this.forwardIndices.size();
    }

    public int getDocId() {
        return docId;
    }

    public HashMap<Integer, Integer> getForwardIndices() {
        return forwardIndices;
    }

    public int getNumWords() {
        return nWords;
    }

    public int getSquareNorm() {
        return squareNorm;
    }
}
