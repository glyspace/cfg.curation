package org.glygen.cfgcuration.model.mapping;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="mapping_tissue", schema="raw_cfg")
public class MappingTissue extends Mapping {
	
}
