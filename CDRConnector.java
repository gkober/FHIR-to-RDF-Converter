package com.gerhard.rdf;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class CDRConnector {
	private FhirContext 		ctx 	= null;
	private String 				  version = null;
	private String 				  server 	= null;
	private IGenericClient 	client 	= null;
	private IBaseBundle 		bundle	= null;
	
	
	public CDRConnector(String version, String server) {
		this.version = version;
		switch (version) {
			case "Dstu3" : 
				this.ctx = FhirContext.forDstu3();
			case "R4" :
				this.ctx = FhirContext.forR4();
		}
		System.out.println(this.server);
		this.server = server;
		System.out.println("setting servername to \"this.server\"");
		System.out.println(this.server);
	}
	public void connect() {
		System.out.println(this.server);
		System.out.println("try to connect");
		this.client = this.ctx.newRestfulGenericClient(this.server);
	}
	public IGenericClient getClient() {
		return this.client;
	}
	public FhirContext getCtx() {
		return this.ctx;
	}
	public IBaseBundle getBundle() {
		return this.bundle;
	}
	public IBaseBundle searchForAllResources(String ResName, int count) {
		switch (ResName) {
			case "Observation":
				if (count>0) {
					this.bundle = this.getClient().search().forResource(Observation.class).count(count).execute();
				}
				else {
					System.out.println("Limit is 0");
					this.bundle = this.getClient().search().forResource(Observation.class).execute();
				}
				break;
			case "Patient":
				this.bundle = this.getClient().search().forResource(Patient.class).execute();
		}
		return this.bundle;
	}
}
