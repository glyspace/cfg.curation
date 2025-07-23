package org.glygen.cfgcuration.glycomedb;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "remote_structure", schema = "remote_one")
public class RemoteStructure {

    @Id
    @Column(name = "remote_structure_id")
    private Integer remoteStructureId;

    @Column(name = "resource")
    private String resource;
    
    @Column(name = "resource_id")
    private String resourceId;

    @ManyToMany
    @JoinTable(
            name = "remote_structure_has_structure",
            schema = "remote_one",
            joinColumns = @JoinColumn(name = "remote_structure_id"),
            inverseJoinColumns = @JoinColumn(name = "structure_id")
        )
    private Set<GlycoCTStructure> structures = new HashSet<>();

	public Integer getRemoteStructureId() {
		return remoteStructureId;
	}

	public void setRemoteStructureId(Integer remoteStructureId) {
		this.remoteStructureId = remoteStructureId;
	}

	public Set<GlycoCTStructure> getStructures() {
		return structures;
	}

	public void setStructures(Set<GlycoCTStructure> structures) {
		this.structures = structures;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	} 
}
