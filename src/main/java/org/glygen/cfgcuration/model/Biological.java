package org.glygen.cfgcuration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="biological", schema="raw_cfg")
public class Biological {
	
	@Id
	String id;
	@Column (name="carb_id")
	String carbId;
	@Column
	String commonname;
	@Column
	String scientificname;
	@Column
	String organ;
	@Column
	String tissue;
	@Column
	String disease;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCarbId() {
		return carbId;
	}
	public void setCarbId(String carb_id) {
		this.carbId = carb_id;
	}
	public String getCommonname() {
		return commonname;
	}
	public void setCommonname(String commonname) {
		this.commonname = commonname;
	}
	public String getScientificname() {
		return scientificname;
	}
	public void setScientificname(String scientificname) {
		this.scientificname = scientificname;
	}
	public String getOrgan() {
		return organ;
	}
	public void setOrgan(String organ) {
		this.organ = organ;
	}
	public String getTissue() {
		return tissue;
	}
	public void setTissue(String tissue) {
		this.tissue = tissue;
	}
	public String getDisease() {
		return disease;
	}
	public void setDisease(String disease) {
		this.disease = disease;
	}
}
