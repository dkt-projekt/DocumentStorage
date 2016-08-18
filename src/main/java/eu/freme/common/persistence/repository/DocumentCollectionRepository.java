package eu.freme.common.persistence.repository;

import org.springframework.data.repository.CrudRepository;

import eu.freme.common.persistence.model.DocumentCollection;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public interface DocumentCollectionRepository extends
		CrudRepository<DocumentCollection, String> {

	DocumentCollection findOneByName(String name);
	
	void deleteByName(String name);
}
