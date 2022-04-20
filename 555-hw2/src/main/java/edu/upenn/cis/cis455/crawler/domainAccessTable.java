//package edu.upenn.cis.cis455.crawler;
//
//import edu.upenn.cis.cis455.storage.DomainAccess;
//import edu.upenn.cis.cis455.storage.Storage;
//
//public class domainAccessTable {
//	Storage db = null;
//	
//	public domainAccessTable(Storage db){
//		this.db = db;
//	}
//	
//	public void addDomain(String domain, int delay) {
//		this.db.addDomain(domain, delay);
//	}
//	
//	public DomainAccess findDomain(String domain) {
//		return this.db.findDomain(domain);
//	}
//	
//	public void updateEntry(String domain) {
//		this.db.updateLastAccessTime(domain);
//	}
//	
//	public int getDelayTime(String domain) {
//		return this.db.getDelayTimeOfDomain(domain);
//	}
//	
//}
