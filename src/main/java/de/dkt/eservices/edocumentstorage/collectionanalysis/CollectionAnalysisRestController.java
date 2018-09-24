package de.dkt.eservices.edocumentstorage.collectionanalysis;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.FREMEHttpException;
import eu.freme.common.exception.OwnedResourceNotFoundException;
import eu.freme.common.persistence.model.CollectionAnalysis;
import eu.freme.common.rest.BaseRestController;

@RestController
public class CollectionAnalysisRestController extends BaseRestController{

    Logger logger = Logger.getLogger(CollectionAnalysisRestController.class);

    @Autowired
    private CollectionAnalysisService collectionAnalysisService;
    
    @PostConstruct
    public void init(){
    }

    @RequestMapping(value = "/collectionAnalysis/{name}", method = RequestMethod.POST)
    public String createCollectionAnalysis(
            @PathVariable("name") String name,
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams
    ) {
        try {

			CollectionAnalysis ca = collectionAnalysisService.createSparqlQuery(name, postBody);
			return "created collection-analysis\"" + name + "\"";
    		
        } catch (AccessDeniedException ex){
            logger.error(ex.getMessage());
            throw new eu.freme.common.exception.AccessDeniedException(ex.getMessage());
        } catch (OwnedResourceNotFoundException | BadRequestException ex){
            logger.error(ex.getMessage());
            throw ex;
        } catch(Exception ex){
            logger.error(ex.getMessage());
            throw new FREMEHttpException(ex.getMessage());
        }
    }
    
    
    @RequestMapping(value = "/collectionAnalysis/{name}", method = RequestMethod.DELETE)
    public String deleteCollectionAnalysis(@PathVariable("name") String name,
    									   @RequestParam Map<String, String> allParams) {
		
    	collectionAnalysisService.deleteSparqlQuery(name);
    	return "deleted collection-analysis\"" + name + "\"";
	
    }
    
	@RequestMapping(value = "/collectionAnalysis", method = RequestMethod.GET)
	public String getCollectionAnalysis() {
		return collectionAnalysisService.listSparqlQueryNames();		
	}
    
    @RequestMapping(value = "/collectionAnalysis/{name}", method = RequestMethod.PUT)
    public String updateCollectionAnalysis(@PathVariable("name") String name,
    									   @RequestParam(value = "query", defaultValue="") String query,
    									   @RequestParam(value = "label", defaultValue="") String label,
    									   @RequestParam Map<String, String> allParams) {
    	
    	
    	if(!query.isEmpty()) {
    		collectionAnalysisService.updateSparqlQuery(name, query);
    	}
    	
    	if(!label.isEmpty()) {
    		collectionAnalysisService.updateSparqlQueryName(name, label);
    	}
    	return "updated collection-analysis\"" + name + "\"";

    }
    
}






