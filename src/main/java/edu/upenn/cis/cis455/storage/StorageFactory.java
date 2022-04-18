package edu.upenn.cis.cis455.storage;

import edu.upenn.cis.cis455.storage.documentStorage.Storage;
import edu.upenn.cis.cis455.storage.documentStorage.StorageInterface;
import edu.upenn.cis.cis455.storage.indexStorage.IndexStorage;

public class StorageFactory {
    static Storage documentDb = null;
    static IndexStorage indexDb = null;
    public static StorageInterface getDocumentDatabase(String directory) {
        if (documentDb!=null){
            return documentDb;
        }
        return new Storage(directory);
    }

    public static IndexStorage getIndexDatabase(String directory){
        if (indexDb != null){
            return indexDb;
        }
        return new IndexStorage(directory);
    }
}
