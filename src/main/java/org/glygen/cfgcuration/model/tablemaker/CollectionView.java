package org.glygen.cfgcuration.model.tablemaker;

import java.util.List;

public class CollectionView {
	Long collectionId;
	String name;
	String description;
	CollectionType type;
	List<Metadata> metadata;
	List<Glycan> glycans;
	List<CollectionTag> tags;
	
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
	public List<Metadata> getMetadata() {
		return metadata;
	}
	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}
	public List<Glycan> getGlycans() {
		return glycans;
	}
	public void setGlycans(List<Glycan> glycans) {
		this.glycans = glycans;
	}
	public Long getCollectionId() {
		return collectionId;
	}
	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}
	public List<CollectionTag> getTags() {
		return tags;
	}
	public void setTags(List<CollectionTag> tags) {
		this.tags = tags;
	}
	public CollectionType getType() {
		return type;
	}
	public void setType(CollectionType type) {
		this.type = type;
	}
}
