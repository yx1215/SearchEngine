package edu.upenn.cis.cis455.storage;

public class StorageFactory {
    public static StorageInterface getDatabaseInstance(String directory) {
        // TODO: factory object, instantiate your storage server
        return new Storage(directory);
    }
}
