package eu.freme.common.persistence.dao;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.freme.common.persistence.model.Document;
import eu.freme.common.persistence.model.Document.Status;
import eu.freme.common.persistence.model.DocumentCollection;
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

		Document doc = list.get(0);

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
	
	/**
	 * Return statistics about how many documents are processed / error / ... in a collection
	 * 
	 * @param dc
	 * @return
	 */
	public HashMap<Document.Status,Integer> getDocumentsStatus(DocumentCollection dc){
		String queryStr = "SELECT status, count(id) FROM document  WHERE collection_name=? GROUP BY status ;";
		Query q = entityManager.createNativeQuery(queryStr);
		q.setParameter(1, dc.getName());
		@SuppressWarnings("rawtypes")
		List list = q.getResultList();
		HashMap<Document.Status,Integer> states = new HashMap<Document.Status, Integer>();
		
		HashMap<Integer,Document.Status> intValues = new HashMap<Integer, Document.Status>(); 
		for( Document.Status s : Document.Status.values() ){
			states.put(s,  0);
			intValues.put(s.ordinal(), s);
		}
		for( int i=0; i<list.size(); i++ ){
			Object[] o = (Object[])list.get(i);
			Document.Status state = intValues.get(o[0]);
			Integer count = ((BigInteger)o[1]).intValue();
			states.put(state,count);
		}
		return states;
	}
	
	/**
	 * This method changes the state of all documents with state CURRENTLY_PROCESSING to UNPROCESSED
	 */
	@Transactional
	public void resetCurrentlyProcessing(){
		String queryStr = "UPDATE document SET status=" + Status.NOT_PROCESSED.ordinal() + " WHERE status=" + Status.CURRENTLY_PROCESSING.ordinal();
		Query q = entityManager.createNativeQuery(queryStr);
		q.executeUpdate();
	}
}
