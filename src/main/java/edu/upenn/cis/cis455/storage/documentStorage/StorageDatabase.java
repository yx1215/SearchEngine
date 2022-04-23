package edu.upenn.cis.cis455.storage.documentStorage;


import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.*;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StorageDatabase {

    private static final String USER_STORE = "user_store";
    private static final String CHANNEL_STORE = "channel_store";
    private static final String ID2CONTENT = "docId2content";
    private static final String URL2ID = "url2docId";

    private static final String CONTENT_SEEN = "content_seen";
    private static final String CLASS_CATALOG = "class_catalog";

    private final Environment env;

    private final Database userDb;

    private final Database docId2contentDb;
    private final Database url2docIdDb;

    private final Database contentSeen;
    private final Database channel;

    private final Database catalogDb;
    private final StoredClassCatalog classCatalog;

    public StorageDatabase(String dir){
        System.out.println("Opening environment in: " + dir);

        File home = new File(dir);
        if (!home.exists()){
            try {
                Files.createDirectory(home.toPath());
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

        DatabaseConfig contentSeenConfig = new DatabaseConfig();
        contentSeenConfig.setTemporary(true);
        contentSeenConfig.setAllowCreate(true);

        this.catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
        this.classCatalog = new StoredClassCatalog(this.catalogDb);

        this.userDb = env.openDatabase(null, USER_STORE, dbConfig);
        this.channel = env.openDatabase(null, CHANNEL_STORE, dbConfig);
        this.docId2contentDb = env.openDatabase(null, ID2CONTENT, dbConfig);
        this.url2docIdDb = env.openDatabase(null, URL2ID, dbConfig);

        this.contentSeen = env.openDatabase(null, CONTENT_SEEN, contentSeenConfig);

    }

    public void close() {

        this.userDb.close();
        this.docId2contentDb.close();
        this.url2docIdDb.close();

        this.channel.close();
        this.contentSeen.close();
        this.classCatalog.close();
        this.env.close();

    }

    public final StoredClassCatalog getClassCatalog() {
        return this.classCatalog;
    }

    public Database getUserDb() {
        return this.userDb;
    }

    public Database getChannel() {
        return channel;
    }

    public Database getContentSeen() {
        return this.contentSeen;
    }

    public Database getDocId2contentDb() {return this.docId2contentDb;}

    public Database getUrl2docIdDb() {
        return this.url2docIdDb;
    }

    public void userDbSync(){
        this.catalogDb.sync();
        this.userDb.sync();
    }

    public void documentDbSync(){
        this.catalogDb.sync();
        this.url2docIdDb.sync();
        this.docId2contentDb.sync();
    }

    public void channelSync(){
        this.catalogDb.sync();
        this.channel.sync();
    }


}
