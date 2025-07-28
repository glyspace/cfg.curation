package org.glygen.cfgcuration.dao;

import java.util.Optional;

import org.glygen.cfgcuration.model.mapping.MappingOrgan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingOrganRepository extends JpaRepository<MappingOrgan, Long> {
	
	Optional<MappingOrgan> findByNameIgnoreCase (String name);
}
