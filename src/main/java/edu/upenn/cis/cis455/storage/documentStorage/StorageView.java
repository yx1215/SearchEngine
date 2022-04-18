package edu.upenn.cis.cis455.storage.documentStorage;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredEntrySet;
import com.sleepycat.collections.StoredSortedMap;
import edu.upenn.cis.cis455.storage.indexStorage.ForwardIndex;
import edu.upenn.cis.cis455.storage.indexStorage.Index;
import edu.upenn.cis.cis455.storage.indexStorage.InvertIndex;

public class StorageView {
    private StoredSortedMap userMap;
    private StoredSortedMap channelMap;
    private StoredSortedMap contentSeenMap;
    private StoredSortedMap docId2contentMap;
    private StoredSortedMap url2docIdMap;

    public StorageView(StorageDatabase db){
        ClassCatalog catalog = db.getClassCatalog();
        EntryBinding usernameBinding = new SerialBinding(catalog, String.class);
        EntryBinding userBinding = new SerialBinding(catalog, User.class);

        EntryBinding channelNameBinding = new SerialBinding(catalog, String.class);
        EntryBinding channelBinding = new SerialBinding(catalog, Channel.class);

        EntryBinding contentSeenKey = new SerialBinding(catalog, byte[].class);
        EntryBinding contentSeenValue = new SerialBinding(catalog, String.class);

        EntryBinding docId1 = new SerialBinding(catalog, Integer.class);
        EntryBinding docContent = new SerialBinding(catalog, DatabaseDocument.class);

        EntryBinding docUrl = new SerialBinding(catalog, String.class);
        EntryBinding docId2 = new SerialBinding(catalog, Integer.class);

        userMap = new StoredSortedMap(db.getUserDb(), usernameBinding, userBinding, true);
        docId2contentMap = new StoredSortedMap(db.getDocId2contentDb(), docId1, docContent, true);
        url2docIdMap = new StoredSortedMap(db.getUrl2docIdDb(), docUrl, docId2, true);

        channelMap = new StoredSortedMap(db.getChannel(), channelNameBinding, channelBinding, true);
        contentSeenMap = new StoredSortedMap(db.getContentSeen(), contentSeenKey, contentSeenValue, true);
    }

    public StoredSortedMap getUserMap() {
        return this.userMap;
    }

    public StoredSortedMap getChannelMap() { return channelMap;}

    public StoredSortedMap getContentSeenMap() {
        return this.contentSeenMap;
    }

    public StoredSortedMap getDocId2contentMap() {return this.docId2contentMap;}

    public StoredSortedMap getUrl2docIdMap() {return this.url2docIdMap;}

    public StoredEntrySet getUserEntrySet(){
        return (StoredEntrySet) this.userMap.entrySet();
    }

    public StoredEntrySet getChannelEntrySet(){ return (StoredEntrySet) this.channelMap.entrySet();}

    public StoredEntrySet getContentSeenEntrySet(){
        return (StoredEntrySet) this.contentSeenMap.entrySet();
    }
}
