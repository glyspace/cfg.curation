package org.glygen.cfgcuration.dao;

import java.util.List;

import org.glygen.cfgcuration.model.Biological;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BiologicalRepository extends JpaRepository<Biological, String> {
	
	@Query("SELECT DISTINCT lower(b.scientificname) FROM Biological b")
	List<String> findDistinctScientificname();
	@Query("SELECT DISTINCT lower(b.tissue) FROM Biological b")
	List<String> findDistinctTissue();
	@Query("SELECT DISTINCT lower(b.organ) FROM Biological b")
	List<String> findDistinctOrgan();
	@Query("SELECT DISTINCT lower(b.disease) FROM Biological b")
	List<String> findDistinctDisease();
	
	long countByScientificnameIgnoreCase (String name);
	long countByTissueIgnoreCase (String name);
	long countByOrganIgnoreCase (String name);
	long countByDiseaseIgnoreCase (String name);

}
