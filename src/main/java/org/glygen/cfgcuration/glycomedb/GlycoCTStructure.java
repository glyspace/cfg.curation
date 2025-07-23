package org.glygen.cfgcuration.glycomedb;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "structure", schema = "core")
public class GlycoCTStructure {
	
	@Column(name="structure_id")
	@Id
	Integer structureId;
	
	@Column(name="glyco_ct", length=15000, nullable=false, unique = true)
	String glycoCT;
	
	@Column(name="sequence_length")
	Integer sequenceLength;
	
	
	@ManyToMany(mappedBy = "structures")
	private Set<RemoteStructure> remoteStructures = new HashSet<>();


	public Integer getStructureId() {
		return structureId;
	}

	public void setStructureId(Integer structureId) {
		this.structureId = structureId;
	}

	public String getGlycoCT() {
		return glycoCT;
	}

	public void setGlycoCT(String glycoCT) {
		this.glycoCT = glycoCT;
	}

	public Integer getSequenceLength() {
		return sequenceLength;
	}

	public void setSequenceLength(Integer sequenceLength) {
		this.sequenceLength = sequenceLength;
	}
}
