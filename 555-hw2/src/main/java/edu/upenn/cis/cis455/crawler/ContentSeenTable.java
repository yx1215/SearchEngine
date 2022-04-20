//package edu.upenn.cis.cis455.crawler;
//
//import edu.upenn.cis.cis455.storage.Storage;
//
//public class ContentSeenTable {
//	Storage db = null;
//	
//	public ContentSeenTable(Storage db) {
//		this.db = db;
//	}
//	
//	public boolean hashContains(byte[] digest) {
//		String hashStr = new String(digest);
//		return this.db.docHashContains(hashStr);
//	}
//
//	public void addHash(byte[] digest) {
//		String hashStr = new String(digest);
//		this.db.addDocHash(hashStr);
//	}
//	
//}
