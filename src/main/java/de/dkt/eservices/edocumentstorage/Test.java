package de.dkt.eservices.edocumentstorage;

import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.freme.common.conversion.rdf.RDFConstants;

public class Test {

	public static void main(String[] args) throws IOException{
		
		
		Model model = ModelFactory.createDefaultModel() ;
		model.read("test.ttl") ;
		
		Property type = model
				.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Resource context = model.getProperty(RDFConstants.nifPrefix + "Context");

		ResIterator itr = model.listResourcesWithProperty(type, context);
		Resource r = itr.next();
		System.err.println(r.getURI());
	}
}
