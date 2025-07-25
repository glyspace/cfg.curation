package org.glygen.cfgcuration.dao;

import java.util.Optional;

import org.glygen.cfgcuration.model.mapping.MappingTissue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MappingTissueRepository extends JpaRepository<MappingTissue, Long> {
	Optional<MappingTissue> findByNameIgnoreCase (String name);
}
