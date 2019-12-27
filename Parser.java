package com.gerhard.rdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.formats.IParser;
import org.hl7.fhir.r4.formats.RdfParser;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.elementmodel.Element;
import org.hl7.fhir.r4.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r4.elementmodel.TurtleParser;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.resultset.TextOutput;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class Parser {	
	static CDRConnector connector = null;
	static IBaseBundle b = null;
	static Model model = null; //apache-jena-model
	public Model convertFhirToRdf(List<Resource> res,int resourcesToCopy) throws IOException {
		RdfParser parser = new RdfParser();
		model = ModelFactory.createDefaultModel();
		model.setNsPrefix("fhir", "http://hl7.org/fhir/");
		//helping to create a new bundle out of the resources, to pass it to the RdfParser
		InputStream in = null;
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
		for (int i=0; i<=resourcesToCopy;i=i+1) {
			for (Resource element : res) {
				bOutput.write(parser.composeString(element).toString().getBytes());
			}
		}
		byte by [] = bOutput.toByteArray();
		in = new ByteArrayInputStream(bOutput.toByteArray());
		model.read(new ByteArrayInputStream(bOutput.toByteArray()),null,"TTL");
		return model;
	}	
	public List<Resource> bundleToResource(Bundle bundle) throws FHIRException, FHIRFormatError, UnsupportedEncodingException, IOException{
		List<Resource> theResources = new ArrayList<Resource>(); 
		for(BundleEntryComponent next : bundle.getEntry()) {
			theResources.add(next.getResource());		
		}
		return theResources;
		
	}
	public void printModel() {
		this.model.write(System.out);
	}
	/** Starting the testprogramm **/
	public static void main(String[] args) throws FHIRException, FHIRFormatError, UnsupportedEncodingException, IOException {
		Parser p = new Parser();
		for (int i = 1; i<=10;i=i+10) {
			long start = System.currentTimeMillis(); 
			connector = new CDRConnector("R4", "http://test.fhir.org/r4");
			connector.connect();
			b = connector.searchForAllResources("Observation",1);
			Bundle bundle = (Bundle)b;
			bundle.getTotal();
			List<Resource> r = p.bundleToResource(bundle);
			for(int j=0;j<=1000;j=j+1000)
				model = p.convertFhirToRdf(r,j);
			model.write(System.out);
	    		Parser.model=null;
		}
		System.exit(0);
	}
	
	

}
