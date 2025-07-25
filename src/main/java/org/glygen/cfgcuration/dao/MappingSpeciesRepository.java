package org.glygen.cfgcuration.dao;

import java.util.Optional;

import org.glygen.cfgcuration.model.mapping.MappingScientificName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingSpeciesRepository extends JpaRepository<MappingScientificName, Long> {
	Optional<MappingScientificName> findByNameIgnoreCase (String name);

}
