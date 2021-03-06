package eu.freme.common.persistence.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.DocumentCollection;

/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public interface DocumentRepository extends CrudRepository<Document, Long> {

	public List<Document> findAllByCollection(DocumentCollection collection);
}
