package de.dkt.eservices.edocumentstorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.conversion.rdf.RDFConversionService;

/**
 * Test rest controller that stores all rdf data it receives in a model and can
 * return this model.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@RestController
public class TestRestController {

	@Autowired
	RDFConversionService rdfConversionService;

	Model model = ModelFactory.createDefaultModel();

	@RequestMapping(value = "/test-endpoint", method = RequestMethod.POST)
	public void addData(@RequestBody String body) throws Exception {
		synchronized (model) {
			Model data = rdfConversionService.unserializeRDF(body,
					RDFSerialization.TURTLE);
			model.add(data);
		}
	}

	@RequestMapping(value = "/test-endpoint", method = RequestMethod.GET)
	public String getData() throws Exception {
		return rdfConversionService
				.serializeRDF(model, RDFSerialization.TURTLE);
	}
}
