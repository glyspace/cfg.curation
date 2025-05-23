package org.glygen.cfgcuration.model;

public class Species {
	
	String id;
	String name;
	String matchedName;
	String rank;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMatchedName() {
		return matchedName;
	}
	public void setMatchedName(String matchedName) {
		this.matchedName = matchedName;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
