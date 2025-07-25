package org.glygen.cfgcuration.model.tablemaker;

import java.util.List;

public class DatasetInputView {
	String name;
	String description;
	String notes;
	License license;
	List<CollectionView> collections;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public License getLicense() {
		return license;
	}
	public void setLicense(License license) {
		this.license = license;
	}
	
	public List<CollectionView> getCollections() {
		return collections;
	}
	public void setCollections(List<CollectionView> collections) {
		this.collections = collections;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
