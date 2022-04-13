package edu.upenn.cis.cis455.storage;


import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StorageDatabase {

    private static final String USER_STORE = "user_store";
    private static final String DOCUMENT_STORE = "document_store";
    private static final String CHANNEL_STORE = "channel_store";

    private static final String CONTENT_SEEN = "content_seen";
    private static final String CLASS_CATALOG = "class_catalog";

    private final Environment env;

    private final Database userDb;
    private final Database documentDb;
    private final Database contentSeen;
    private final Database channel;

    private final Database catalogDb;
    private final StoredClassCatalog classCatalog;

    public StorageDatabase(String dir){
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

        DatabaseConfig contentSeenConfig = new DatabaseConfig();
        contentSeenConfig.setTemporary(true);
        contentSeenConfig.setAllowCreate(true);

        this.catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
        this.classCatalog = new StoredClassCatalog(this.catalogDb);

        this.userDb = env.openDatabase(null, USER_STORE, dbConfig);
        this.documentDb = env.openDatabase(null, DOCUMENT_STORE, dbConfig);
        this.channel = env.openDatabase(null, CHANNEL_STORE, dbConfig);

        this.contentSeen = env.openDatabase(null, CONTENT_SEEN, contentSeenConfig);

    }

    public void close() {

        this.userDb.close();
        this.documentDb.close();
        this.channel.close();
        this.contentSeen.close();
        this.classCatalog.close();
        this.env.close();

    }

    public final StoredClassCatalog getClassCatalog() {
        return this.classCatalog;
    }

    public Database getDocumentDb() {
        return this.documentDb;
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

    public void userDbSync(){
        this.catalogDb.sync();
        this.userDb.sync();
    }

    public void documentDbSync(){
        this.catalogDb.sync();
        this.documentDb.sync();
    }

    public void channelSync(){
        this.catalogDb.sync();
        this.channel.sync();
    }

}
