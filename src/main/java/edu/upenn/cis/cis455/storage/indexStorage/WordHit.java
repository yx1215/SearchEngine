package edu.upenn.cis.cis455.storage.indexStorage;

import java.io.Serializable;

public class WordHit implements Serializable {

    private final boolean isCapital;
    private final int pos;

    public WordHit(int pos, boolean isCapital){
        this.isCapital = isCapital;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public boolean isCapital() {
        return isCapital;
    }
}
