package org.glygen.cfgcuration.dao2;

import java.util.Optional;

import org.glygen.cfgcuration.glycomedb.RemoteStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RemoteStructureRepository extends JpaRepository<RemoteStructure, Integer> {
	Optional<RemoteStructure> findByResourceId(String resourceId);
	

	@Query("SELECT r FROM RemoteStructure r JOIN FETCH r.structures WHERE r.resourceId = :id")
	Optional<RemoteStructure> findByResourceIdWithStructures(@Param("id") String id);

}
