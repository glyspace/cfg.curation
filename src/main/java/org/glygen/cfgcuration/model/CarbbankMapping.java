package org.glygen.cfgcuration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="carbbank_mapping")
public class CarbbankMapping {
	@Id
	String id;
	@Column
	String carb_id;
	@Column
	String carbbank_id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCarb_id() {
		return carb_id;
	}
	public void setCarb_id(String carb_id) {
		this.carb_id = carb_id;
	}
	public String getCarbbank_id() {
		return carbbank_id;
	}
	public void setCarbbank_id(String carbbank_id) {
		this.carbbank_id = carbbank_id;
	}
}
