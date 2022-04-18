package edu.upenn.cis.cis455.storage.documentStorage;

import edu.upenn.cis.cis455.storage.indexStorage.ForwardIndex;
import edu.upenn.cis.cis455.storage.indexStorage.Index;
import edu.upenn.cis.cis455.storage.indexStorage.InvertIndex;
import edu.upenn.cis.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis.cis455.xpathengine.XPathEngineImp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Storage implements StorageInterface{
    /**
     * How many documents so far?
     */
    final static Logger logger = LogManager.getLogger(Storage.class);

    public final StorageDatabase storageDatabase;
    public final StorageView storageView;

    public String dir;

    public AtomicInteger docId;

    public Storage(String dir){
        this.storageDatabase = new StorageDatabase(dir);
        this.storageView = new StorageView(this.storageDatabase);
        this.dir = dir;
        this.docId = new AtomicInteger(this.storageView.getDocId2contentMap().size());
    }

    @Override
    public int getCorpusSize() {
        return this.storageView.getDocId2contentMap().size();
    }

    /**
     * Add a new document, getting its ID
     *
     * @param url
     * @param documentContents
     */
    @Override
    public int addDocument(String url, String documentContents, String contentType) {
        DatabaseDocument databaseDocument = new DatabaseDocument(url, documentContents, contentType);
        Map docId2content = this.storageView.getDocId2contentMap();
        Map url2docId = this.storageView.getUrl2docIdMap();
        int docId;
        if (url2docId.containsKey(url)){
            docId = (int) url2docId.get(url);
        } else {
            docId = this.docId.getAndIncrement();
            url2docId.put(url, docId);
        }
        docId2content.put(docId, databaseDocument);
        this.storageDatabase.documentDbSync();
        return docId;
    }

    /**
     * Retrieves a document's contents by URL
     *
     * @param url
     */
    @Override
    public DatabaseDocument getDocument(String url) {
        Map docId2content = this.storageView.getDocId2contentMap();
        Map url2docId = this.storageView.getUrl2docIdMap();
        if (!url2docId.containsKey(url)){
            return null;
        }
        int docId = (int) url2docId.get(url);
        DatabaseDocument databaseDocument = (DatabaseDocument) docId2content.get(docId);
        return databaseDocument;
    }

    public DatabaseDocument getDocById(int docId){
        Map docId2content = this.storageView.getDocId2contentMap();
        if (!docId2content.containsKey(docId)){
            return null;
        }

        return (DatabaseDocument) docId2content.get(docId);
    }

    public String getUrlById(int docId){
        Map url2docId = this.storageView.getUrl2docIdMap();
        for (Object url: url2docId.keySet()){
            if ((int) url2docId.get(url) == docId){
                return (String) url;
            }
        }
        return null;
    }

    public int getDocId(String url){
        Map url2docId = this.storageView.getUrl2docIdMap();
        if (!url2docId.containsKey(url)){
            return -1;
        }
        return (int) url2docId.get(url);
    }
    /**
     * Adds a user and returns an ID
     *
     * @param username
     * @param password
     */
    @Override
    public void addUser(String username, String password) {
        Map userMap = this.storageView.getUserMap();
        User user = new User(username, password);
        userMap.put(username, user);
        this.storageDatabase.userDbSync();
    }

    public User getUser(String username){
        Map userMap = this.storageView.getUserMap();
        return (User) userMap.get(username);
    }

    /**
     * Tries to log in the user, or else throws a HaltException
     *
     * @param username
     * @param password
     */
    @Override
    public boolean getSessionForUser(String username, String password) {
        byte[] pwdHash = this.getPwd(username);
        try {
            if (pwdHash == null) {
                logger.info("Username " + username + " does not exist.");
                return false;
            } else {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] pwdHashGiven = digest.digest(password.getBytes(StandardCharsets.UTF_8));
                if (!Arrays.equals(pwdHash, pwdHashGiven)){
                    logger.info("Incorrect password");
                    return false;
                }
                return true;
            }
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Shuts down / flushes / closes the storage system
     */
    @Override
    public void close() {
        this.storageDatabase.close();
    }

    public byte[] getPwd(String username){
        Map userMap = this.storageView.getUserMap();
        User user = (User) userMap.get(username);
        return user == null ? null : user.getPwdHash();
    }

    public String checkContent(String url, String content){
        Map contentSeenMap = this.storageView.getContentSeenMap();
//        logger.info("Content Seen size: " + contentSeenMap.size());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] contentHash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            if (contentSeenMap.containsKey(contentHash)){
                return (String) contentSeenMap.get(contentHash);
            } else {
                contentSeenMap.put(contentHash, url);
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public void updateChannel(String channelName, String url){
        Channel channel = this.getChannel(channelName);
        if (channel != null){
            channel.addUrl(url);
            Map channelMap = this.storageView.getChannelMap();
            channelMap.put(channelName, channel);
            this.storageDatabase.channelSync();
        }
        else {
            logger.warn("Channel " + channelName + " does not exist");
        }
    }

    public void addChannel(String user, String channelName, String XPath) throws Exception{
        XPathEngineImp tmpEngine = (XPathEngineImp) XPathEngineFactory.getXPathEngine();
        // this function as checking the validity of the xpath.
        tmpEngine.addXPath(XPath);
        Channel channel = new Channel(channelName, XPath, user);
        Map channelMap = this.storageView.getChannelMap();
        channelMap.put(channelName, channel);
        this.storageDatabase.channelSync();
    }

    public Channel getChannel(String channelName){
        Map channelMap = this.storageView.getChannelMap();
        return (Channel) channelMap.get(channelName);
    }

    public void subscribeChannel(String username, String channelName){
        Map userMap = this.storageView.getUserMap();
        Map channelMap = this.storageView.getChannelMap();
        if (userMap.get(username) == null){
            throw new InvalidParameterException("User: " + username + " does not exist.");
        }

        if (channelMap.get(channelName) == null){
            throw new InvalidParameterException("Channel: " + channelName + " does not exist.");
        }

        User user = (User) userMap.get(username);

        if (user.getSubscriptions().contains(channelName)){
            throw new InvalidParameterException("User " + username + " has already subscribed channel " + channelName);
        }

        user.subscribe(channelName);
        userMap.put(username, user);
        this.storageDatabase.userDbSync();
    }

    @Override
    public void addAdmin(){
        Map userMap = this.storageView.getUserMap();
        User admin = User.createAdmin();
        if (!userMap.containsKey(admin.getUsername())){
            userMap.put(admin.getUsername(), admin);
            this.storageDatabase.userDbSync();
        }
    }

    public Set getAllChannels(){
        return this.storageView.getChannelMap().keySet();
    }

    public Set getAllUrl(){ return this.storageView.getUrl2docIdMap().keySet(); }
}
