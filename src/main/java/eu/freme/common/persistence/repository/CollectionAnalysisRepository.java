package eu.freme.common.persistence.repository;

import eu.freme.common.persistence.model.CollectionAnalysis;
import org.springframework.data.repository.CrudRepository;

public interface CollectionAnalysisRepository extends CrudRepository<CollectionAnalysis, String> {
    CollectionAnalysis findOneByName(String name);

	void deleteByName(String name);

}

