package edu.upenn.cis.cis455.storage.indexStorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Index implements Serializable {


    private final int wordId;
    private final int docId;

    private ArrayList<WordHit> hits = new ArrayList<>();

    public Index(int wordId, int docId){
        this.wordId = wordId;
        this.docId = docId;
    }

    public void addHit(int pos, String orig){
        boolean isCapital = Character.isUpperCase(orig.charAt(0));
        hits.add(new WordHit(pos, isCapital));
    }

    public void addHits(String word, Collection<Integer> collection){
        for (int pos: collection){
            addHit(pos, word);
        }
    }



    public ArrayList<WordHit> getHits() {
        return hits;
    }

    public int getDocId() {
        return docId;
    }

    public int getWordId() {
        return wordId;
    }

    public int getNumHits(){
        return hits.size();
    }

    public String getString(){
        return "" + this.docId + "@" + this.wordId;
    }
}
