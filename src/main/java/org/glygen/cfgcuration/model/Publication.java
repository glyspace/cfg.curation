package org.glygen.cfgcuration.model;

import org.apache.commons.text.similarity.LevenshteinDistance;

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
			if (title != null && ((Publication) obj).getTitle() != null && almostIdentical(title, ((Publication) obj).getTitle())) {
				if (author != null) {
					if (authorMatch(((Publication) obj).getAuthor())) {
						return journalMatch (((Publication) obj));
					}
				} else {
					return journalMatch (((Publication) obj));
				}
			} 
		}
		return super.equals(obj);
	}
	
	public static boolean almostIdentical (String text1, String text2) {
		
		// Normalize
		String norm1 = normalize(text1);
		String norm2 = normalize(text2);
		
		LevenshteinDistance ld = new LevenshteinDistance();
		int distance = ld.apply(norm1, norm2);

		// Similarity score
		int maxLen = Math.max(norm1.length(), norm2.length());
		double similarity = 1.0 - (double) distance / maxLen;
		
		if (similarity > 0.9)
			return true;
		return false;
		
	}
	

	private static String normalize(String input) {
		return input.toLowerCase()
				.replaceAll("[^a-z0-9 ]", "") 
				.replaceAll("\\s+", " ").trim();
	}
	
	public boolean journalMatch(Publication publication) {
		if (this.journalName != null && publication.getJournalName() != null && almostIdentical (this.journalName, publication.getJournalName())) {
			// check if at least one of year or volume or page range matches
			if (this.year != null && this.year.equalsIgnoreCase(publication.getYear())) {
				return true;
			}
			if (this.volume != null && this.volume.equalsIgnoreCase(publication.getVolume())) {
				return true;
			}
			if (this.pageRange != null && this.pageRange.equalsIgnoreCase(publication.getPageRange())) {
				return true;
			}
		} else {
			// check if others match
			if (this.year != null && this.year.equalsIgnoreCase(publication.getYear())) {
				if (this.volume != null && this.volume.equalsIgnoreCase(publication.getVolume())) {
					return true;
				}
				if (this.pageRange != null && this.pageRange.equalsIgnoreCase(publication.getPageRange())) {
					return true;
				}
			} else {
				if (this.volume != null && this.volume.equalsIgnoreCase(publication.getVolume())) {
					if (this.pageRange != null && this.pageRange.equalsIgnoreCase(publication.getPageRange())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean authorMatch (String author2) {
		if (this.author != null && this.author.equalsIgnoreCase(author2))
			return true;
		if (this.author != null && author2 != null) {
			// use only last names to match
			String[] authorList = this.author.split(";");
			String lastNames1 = "";
			for (String a: authorList) {
				String trimmed = a.trim();
				if (trimmed.indexOf(" ") != -1) {
					trimmed =trimmed.substring(0, trimmed.lastIndexOf(" "));
				}
				lastNames1 += trimmed.trim() + ";";
			}
			authorList = author2.split(";");
			String lastNames2 = "";
			for (String a: authorList) {
				String trimmed = a.trim();
				if (trimmed.indexOf(" ") != -1) {
					trimmed = trimmed.substring(0, trimmed.lastIndexOf(" "));
				}
				lastNames2 += trimmed.trim() + ";";
			}
			return almostIdentical(lastNames1, lastNames2);
		}
		if (this.author == null && author2 == null) return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		String pub = title+author+journalName+year+pageRange;
		return pub.hashCode();
	}
}
