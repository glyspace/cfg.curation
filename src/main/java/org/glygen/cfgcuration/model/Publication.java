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
	
	@Column
	String pmid;
	@Column
	String doiId;
	@Column
	Boolean checked;
	@Column
	String matchCount;
	@Column(length=4000)
	String matchDetails;
	
	
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
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
	public String getDoiId() {
		return doiId;
	}
	public void setDoiId(String doiId) {
		this.doiId = doiId;
	}
	public Boolean getChecked() {
		return checked;
	}
	public void setChecked(Boolean checked) {
		this.checked = checked;
	}
	public String getMatchCount() {
		return matchCount;
	}
	public void setMatchCount(String matchCount) {
		this.matchCount = matchCount;
	}
	public String getMatchDetails() {
		return matchDetails;
	}
	public void setMatchDetails(String matchDetails) {
		this.matchDetails = matchDetails;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Publication) {
			if (title != null && title.equalsIgnoreCase(((Publication) obj).getTitle())) {
				if (author != null) {
					if (author.equalsIgnoreCase(((Publication) obj).getAuthor())) {
						if (journalName != null) {
							if (journalName.equalsIgnoreCase(((Publication) obj).getJournalName())) {
								if (year != null) {
									if (year.equalsIgnoreCase(((Publication) obj).getYear())) {
										if (pageRange != null) {
											return pageRange.equalsIgnoreCase(((Publication) obj).getPageRange());
										} else {
											return true;
										}
									}
								} else {
									return true;
								}
							}
						}
						else {
							return true;
						}
					}
				} else {
					if (journalName != null) {
						if (journalName.equalsIgnoreCase(((Publication) obj).getJournalName())) {
							if (year != null) {
								if (year.equalsIgnoreCase(((Publication) obj).getYear())) {
									if (pageRange != null) {
										return pageRange.equalsIgnoreCase(((Publication) obj).getPageRange());
									} else {
										return true;
									}
								}
							} else {
								return true;
							}
						}
					}
					else {
						return true;
					}
				}
			}
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		String pub = title+author+journalName+year+pageRange;
		return pub.hashCode();
	}
}
