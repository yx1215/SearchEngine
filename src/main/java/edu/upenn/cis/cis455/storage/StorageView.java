package edu.upenn.cis.cis455.storage;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredEntrySet;
import com.sleepycat.collections.StoredSortedMap;

public class StorageView {
    private StoredSortedMap userMap;
    private StoredSortedMap documentMap;
    private StoredSortedMap channelMap;
    private StoredSortedMap contentSeenMap;

    public StorageView(StorageDatabase db){
        ClassCatalog catalog = db.getClassCatalog();
        EntryBinding usernameBinding = new SerialBinding(catalog, String.class);
        EntryBinding userBinding = new SerialBinding(catalog, User.class);

        EntryBinding urlBinding = new SerialBinding(catalog, String.class);
        EntryBinding documentContentBinding = new SerialBinding(catalog, DatabaseDocument.class);

        EntryBinding channelNameBinding = new SerialBinding(catalog, String.class);
        EntryBinding channelBinding = new SerialBinding(catalog, Channel.class);

        EntryBinding contentSeenKey = new SerialBinding(catalog, byte[].class);
        EntryBinding contentSeenValue = new SerialBinding(catalog, String.class);

        userMap = new StoredSortedMap(db.getUserDb(), usernameBinding, userBinding, true);
        documentMap = new StoredSortedMap(db.getDocumentDb(), urlBinding, documentContentBinding, true);
        channelMap = new StoredSortedMap(db.getChannel(), channelNameBinding, channelBinding, true);
        contentSeenMap = new StoredSortedMap(db.getContentSeen(), contentSeenKey, contentSeenValue, true);
    }

    public StoredSortedMap getUserMap() {
        return this.userMap;
    }

    public StoredSortedMap getDocumentMap() {
        return this.documentMap;
    }

    public StoredSortedMap getChannelMap() { return channelMap;}

    public StoredSortedMap getContentSeenMap() {
        return this.contentSeenMap;
    }

    public StoredEntrySet getUserEntrySet(){
        return (StoredEntrySet) this.userMap.entrySet();
    }

    public StoredEntrySet getDocumentEntrySet(){
        return (StoredEntrySet) this.documentMap.entrySet();
    }

    public StoredEntrySet getChannelEntrySet(){ return (StoredEntrySet) this.channelMap.entrySet();}

    public StoredEntrySet getContentSeenEntrySet(){
        return (StoredEntrySet) this.contentSeenMap.entrySet();
    }
}
