package org.glygen.cfgcuration.dao;

import org.glygen.cfgcuration.model.mapping.MappingScientificName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingSpeciesRepository extends JpaRepository<MappingScientificName, Long> {

}
