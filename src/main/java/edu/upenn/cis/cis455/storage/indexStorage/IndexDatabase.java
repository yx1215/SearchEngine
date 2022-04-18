package edu.upenn.cis.cis455.storage.indexStorage;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class IndexDatabase {

    private static final String CLASS_CATALOG = "class_catalog";
    private static final String WORD2ID = "word2wordId";
    private static final String INDEXSTRING2ID = "indexString2indexId";
    private static final String INDEXID2INDEX = "indexId2Index";
    private static final String FORWARD = "forwardIndex";
    private static final String INVERT = "invertIndex";

    private final Environment env;

    private final Database word2wordId;
    private final Database indexString2indexId;
    private final Database indexId2index;
    private final Database forwardIndicesDb;
    private final Database invertIndicesDb;

    private final Database catalogDb;
    private final StoredClassCatalog classCatalog;

    public IndexDatabase(String dir){
        System.out.println("Opening environment in: " + dir);

        File home = new File(dir);
        if (!home.exists()){
            try {
                Files.createDirectory(Path.of(dir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        this.env = new Environment(new File(dir), envConfig);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(true);

        this.catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
        this.classCatalog = new StoredClassCatalog(this.catalogDb);

        this.word2wordId = env.openDatabase(null, WORD2ID, dbConfig);
        this.indexString2indexId = env.openDatabase(null, INDEXSTRING2ID, dbConfig);
        this.indexId2index = env.openDatabase(null, INDEXID2INDEX, dbConfig);
        this.forwardIndicesDb = env.openDatabase(null, FORWARD, dbConfig);
        this.invertIndicesDb = env.openDatabase(null, INVERT, dbConfig);

    }

    public void close(){
        this.indexString2indexId.close();
        this.indexId2index.close();
        this.word2wordId.close();
        this.forwardIndicesDb.close();
        this.invertIndicesDb.close();
        this.classCatalog.close();
    }

    public void sync(){
        this.catalogDb.sync();
        this.indexId2index.sync();
        this.indexString2indexId.sync();
        this.word2wordId.sync();
        this.forwardIndicesDb.sync();
        this.invertIndicesDb.sync();
    }

    public Database getIndexId2index() {
        return indexId2index;
    }

    public Database getIndexString2indexId() {
        return indexString2indexId;
    }

    public Database getInvertIndicesDb() {
        return invertIndicesDb;
    }

    public Database getForwardIndicesDb() {
        return forwardIndicesDb;
    }

    public Database getWord2wordId() {
        return word2wordId;
    }

    public final StoredClassCatalog getClassCatalog() {
        return this.classCatalog;
    }
}
