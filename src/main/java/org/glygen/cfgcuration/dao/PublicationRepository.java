package org.glygen.cfgcuration.dao;

import java.util.List;

import org.glygen.cfgcuration.model.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
	
	List<Publication> findByCarbKey (String carbKey);

}
