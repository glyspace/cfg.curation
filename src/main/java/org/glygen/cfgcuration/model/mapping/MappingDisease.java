package org.glygen.cfgcuration.model.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name="mapping_disease")
public class MappingDisease {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "publication_seq")
	@SequenceGenerator(
			name = "publication_seq",
			sequenceName = "raw_cfg.publication_seq", 
			allocationSize = 1)
	Long id;
	@Column
	String name;
	@Column
	Integer count;
	@Column
	String namespaceName;
	@Column
	String namespaceId;
	@Column
	String mappingName;
	
	@Column
	String matchCount;
	
	public String getMatchCount() {
		return matchCount;
	}
	
	public void setMatchCount(String matchCount) {
		this.matchCount = matchCount;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getNamespaceName() {
		return namespaceName;
	}
	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}
	public String getNamespaceId() {
		return namespaceId;
	}
	public void setNamespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
	}
	public String getMappingName() {
		return mappingName;
	}
	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}

}
