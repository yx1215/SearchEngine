package edu.upenn.cis.cis455.xpathengine;

/**
 This class encapsulates the tokens we care about parsing in XML (or HTML)
 */
public class OccurrenceEvent {

	public enum Type {Open, Close, Text};
	
	Type type;
	String value;
	String text = null;
	int level;

	public OccurrenceEvent(Type t, String value, int level) {
		this.type = t;
		this.value = value;
		this.level = level;
	}

	public OccurrenceEvent(Type t, String value, int level, String text) {
		this.type = t;
		this.value = value;
		this.level = level;
		this.text = text;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public String toString() {
		if (type == Type.Open)
			return "Open:" + value + ", level: " + this.level;
		else if (type == Type.Close)
			return "Close:" + value + ", level: " + this.level;
		else
			return "Text: " + this.text + ", level: " + this.level;
	}

	@Override
	public boolean equals(Object o){
		if (!o.getClass().equals(this.getClass())){
			return false;
		}

		OccurrenceEvent other = (OccurrenceEvent) o;
		boolean textEqual = this.text == null ? other.getText() == null : this.text.equals(other.getText());

		return this.type.equals(other.getType()) &&
				this.value.equals(other.getValue()) &&
				textEqual &&
				this.level == other.getLevel();
	}
}
