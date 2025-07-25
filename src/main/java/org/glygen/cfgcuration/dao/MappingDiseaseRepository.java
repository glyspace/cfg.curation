package org.glygen.cfgcuration.dao;

import java.util.Optional;

import org.glygen.cfgcuration.model.mapping.MappingDisease;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingDiseaseRepository extends JpaRepository<MappingDisease, Long> {
	Optional<MappingDisease> findByNameIgnoreCase (String name);
}
