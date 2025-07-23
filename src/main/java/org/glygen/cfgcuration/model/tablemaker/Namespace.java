package org.glygen.cfgcuration.model.tablemaker;

public class Namespace {
	
	Long namespaceId;
	String name;
	String description;
	String dictionary;
	Boolean hasUri = false;
	Boolean hasId = false;
	String fileIdentifier;
	
	public Long getNamespaceId() {
		return namespaceId;
	}
	public void setNamespaceId(Long namespaceId) {
		this.namespaceId = namespaceId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDictionary() {
		return dictionary;
	}
	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}
	
	public Boolean getHasUri() {
		return hasUri;
	}
	public void setHasUri(Boolean hasUri) {
		this.hasUri = hasUri;
	}
	
	public Boolean getHasId() {
		return hasId;
	}
	public void setHasId(Boolean hasId) {
		this.hasId = hasId;
	}
	
	public String getFileIdentifier() {
		return fileIdentifier;
	}
	public void setFileIdentifier(String fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
