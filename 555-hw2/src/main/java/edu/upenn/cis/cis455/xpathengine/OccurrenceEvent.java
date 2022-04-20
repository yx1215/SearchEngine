package edu.upenn.cis.cis455.xpathengine;

/**
 This class encapsulates the tokens we care about parsing in XML (or HTML)
 */
public class OccurrenceEvent {
	public static enum Type {Open, Close, Text};
	
	public Type type;
	String value;
	String documentUrl;
	int level = 0;
	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}


	String docType;
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public OccurrenceEvent(Type t, String value) {
		this.type = t;
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getDocumentUrl() {
		return documentUrl;
	}

	public void setDocumentUrl(String documentUrl) {
		this.documentUrl = documentUrl;
	}
	

	public String toString() {
		if (type == Type.Open) 
			return "<" + value + ">";
		else if (type == Type.Close)
			return "</" + value + ">";
		else
			return value;
	}
}
