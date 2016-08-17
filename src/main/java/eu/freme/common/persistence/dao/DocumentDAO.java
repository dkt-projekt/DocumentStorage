package eu.freme.common.persistence.dao;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.Document.Status;
import eu.freme.common.persistence.repository.DocumentRepository;

/**
 * Complex database functionality for documents
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Component
public class DocumentDAO {

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	EntityManager entityManager;

	/**
	 * Select a document to be processed by the pipeline and set its state to
	 * CURRENTLY_PROCESSING
	 */
	@Transactional
	public Document fetchNextForProcessing() {

		TypedQuery<Document> query = entityManager.createQuery(
				"SELECT c FROM Document c WHERE c.status="
						+ Document.Status.NOT_PROCESSED.ordinal(),
				Document.class).setMaxResults(1);
		List<Document> list = query.getResultList();

		if (list.size() == 0) {
			return null;
		}

		Document doc = (Document) list.get(0);

		doc.setStatus(Status.CURRENTLY_PROCESSING);
		doc.setLastUpdate(new Date());
		documentRepository.save(doc);
		return doc;
	}

	/**
	 * Set the document to the error state, setting the error message also
	 * 
	 * @param doc
	 * @param errorMessage
	 */
	public void setErrorState(Document doc, String errorMessage) {
		doc.setErrorMessage(errorMessage);
		doc.setStatus(Status.ERROR);
		documentRepository.save(doc);
	}
}
