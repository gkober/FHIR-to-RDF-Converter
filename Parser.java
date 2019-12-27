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
		long start = System.currentTimeMillis(); 
		long startCreateParser = System.currentTimeMillis(); 
		RdfParser parser = new RdfParser();
		long endCreateParser = System.currentTimeMillis(); 
		long startCreateModel = System.currentTimeMillis(); 
		model = ModelFactory.createDefaultModel();
		model.setNsPrefix("fhir", "http://hl7.org/fhir/");
		long endCreateModel = System.currentTimeMillis(); 
		//helping to create a new bundle out of the resources, to pass it to the RdfParser
		//Bundle b = new Bundle();
		InputStream in = null;
		ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
		long start1 = System.currentTimeMillis(); 
		for (int i=0; i<=resourcesToCopy;i=i+1) {
			for (Resource element : res) {
				bOutput.write(parser.composeString(element).toString().getBytes());
				//System.out.println(parser.composeString(element).toString());
			}
		}
		long start2 = System.currentTimeMillis(); 
		byte by [] = bOutput.toByteArray();
		//in = new ByteArrayInputStream(by);
		in = new ByteArrayInputStream(bOutput.toByteArray());
		//System.out.println(connector.getCtx().newXmlParser().setPrettyPrint(true).encodeResourceToString(b));
		long start3 = System.currentTimeMillis(); 
		model.read(new ByteArrayInputStream(bOutput.toByteArray()),null,"TTL");
		//model.read(new ByteArrayInputStream(bOutput.toByteArray()),null,"JSONLD");
		long end = System.currentTimeMillis(); 
		//System.out.println("convert - Time taken complete: " +  (end - start) + "ms" + " for " + resourcesToCopy +
		//		 " entries"); 
		//System.out.println("convert - Time taken reading resources: " +  (start2 - start1) + "ms"); 
		//System.out.println("convert - Time taken creating the model: " +  (endCreateModel - startCreateModel) + "ms"); 
		//System.out.println("convert - Time taken creating the parser: " +  (endCreateParser - startCreateParser) + "ms"); 
		System.out.println("convert - Time taken reading inputStream to model: " +  (end - start3) + "ms" + " for " + resourcesToCopy +
				 " entries"); 
		//model.read(in,null,"TTL");
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
			long endconnection = System.currentTimeMillis(); 
			long startconversion = System.currentTimeMillis(); 
			for(int j=0;j<=1000;j=j+1000)
			model = p.convertFhirToRdf(r,j);
			model.write(System.out);
			long end = System.currentTimeMillis(); 
	    System.out.println("Time taken complete: " +  (end - start) + "ms"); 
	    System.out.println("Time connection taken: " +  (endconnection - start) + "ms");
	    System.out.println("Time conversion taken: " +  (end - startconversion) + "ms"); 
	    System.out.println("=================");
	    Parser.model=null;
		}
		System.exit(0);
	}
	
	

}
