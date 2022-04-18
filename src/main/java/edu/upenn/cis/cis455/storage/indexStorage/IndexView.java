package edu.upenn.cis.cis455.storage.indexStorage;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredSortedMap;

public class IndexView {

    private StoredSortedMap word2wordIdMap;
    private StoredSortedMap indexString2indexIdMap;
    private StoredSortedMap indexId2IndexMap;
    private StoredSortedMap forwardIndexMap;
    private StoredSortedMap invertIndexMap;

    public IndexView(IndexDatabase db){
        ClassCatalog catalog = db.getClassCatalog();

        EntryBinding word = new SerialBinding(catalog, String.class);
        EntryBinding wordId = new SerialBinding(catalog, Integer.class);

        EntryBinding indexString = new SerialBinding(catalog, String.class);
        EntryBinding indexId = new SerialBinding(catalog, Integer.class);

        EntryBinding indexId2 = new SerialBinding(catalog, Integer.class);
        EntryBinding index = new SerialBinding(catalog, Index.class);

        EntryBinding forwardDocId = new SerialBinding(catalog, Integer.class);
        EntryBinding forwardIndex = new SerialBinding(catalog, ForwardIndex.class);

        EntryBinding invertWordId = new SerialBinding(catalog, Integer.class);
        EntryBinding invertIndex = new SerialBinding(catalog, InvertIndex.class);

        word2wordIdMap = new StoredSortedMap(db.getWord2wordId(), word, wordId, true);
        indexString2indexIdMap = new StoredSortedMap(db.getIndexString2indexId(), indexString, indexId, true);
        indexId2IndexMap = new StoredSortedMap(db.getIndexId2index(), indexId2, index, true);
        forwardIndexMap = new StoredSortedMap(db.getForwardIndicesDb(), forwardDocId, forwardIndex, true);
        invertIndexMap = new StoredSortedMap(db.getInvertIndicesDb(), invertWordId, invertIndex, true);
    }
    public StoredSortedMap getForwardIndexMap() {
        return forwardIndexMap;
    }

    public StoredSortedMap getInvertIndexMap() {
        return invertIndexMap;
    }

    public StoredSortedMap getWord2wordIdMap() {
        return word2wordIdMap;
    }

    public StoredSortedMap getIndexString2indexIdMap() {
        return indexString2indexIdMap;
    }

    public StoredSortedMap getIndexId2IndexMap() {
        return indexId2IndexMap;
    }
}
