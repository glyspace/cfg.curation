package org.glygen.cfgcuration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="structures")
public class Structures {
	@Id
	String id;
	@Column
	String carb_id;
	@Column(length=4000)
	String linearcode;
	@Column(length=4000)
	String iupac;
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
	public String getLinearcode() {
		return linearcode;
	}
	public void setLinearcode(String linearcode) {
		this.linearcode = linearcode;
	}
	public String getIupac() {
		return iupac;
	}
	public void setIupac(String iupac) {
		this.iupac = iupac;
	}

}
