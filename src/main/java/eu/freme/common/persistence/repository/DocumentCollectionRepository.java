package eu.freme.common.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public interface DocumentCollectionRepository extends
		CrudRepository<DocumentCollection, String> {

	DocumentCollection findOneByName(String name);
	
	void deleteByName(String name);
	
	  @Query(value = "UPDATE document SET error_message=NULL, status=0 WHERE collection_name=?1 and status=3;", nativeQuery = true)
	  void resetErrorStates(String collectionName);

}
