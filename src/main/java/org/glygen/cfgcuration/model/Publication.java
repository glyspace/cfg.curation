package org.glygen.cfgcuration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Publication {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "publication_seq")
	@SequenceGenerator(
			name = "publication_seq",
			sequenceName = "raw_cfg.publication_seq", 
			allocationSize = 1)
	Long id;
	
	@Column
	String carbKey;
	@Column
	String journalKey;
	@Column
	String journalId;
	@Column(length=4000)
	String title;
	@Column
	String pageRange;
	@Column
	String journalName;
	@Column
	String year;
	
	@Column
	String journalIdType;
	@Column(length=2000)
	String author;
	@Column
	String volume;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCarbKey() {
		return carbKey;
	}
	public void setCarbKey(String carbKey) {
		this.carbKey = carbKey;
	}
	public String getJournalKey() {
		return journalKey;
	}
	public void setJournalKey(String journalKey) {
		this.journalKey = journalKey;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPageRange() {
		return pageRange;
	}
	public void setPageRange(String pageRange) {
		this.pageRange = pageRange;
	}
	public String getJournalName() {
		return journalName;
	}
	public void setJournalName(String journalName) {
		this.journalName = journalName;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getJournalId() {
		return journalId;
	}
	public void setJournalId(String journalId) {
		this.journalId = journalId;
	}
	public String getJournalIdType() {
		return journalIdType;
	}
	public void setJournalIdType(String journalIdType) {
		this.journalIdType = journalIdType;
	}
}
