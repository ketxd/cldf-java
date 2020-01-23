package de.tuebingen.sfs.cldfjava.lexdata;

import java.util.HashMap;
import java.util.Map;

public class CLDFParameter {
	String paramID; //the foreign key used by the rest of the database
	String name;
	String concepticonID;
	Map<String,String> properties; //to store additional info
	
	public CLDFParameter() {
		paramID = "";
		name = "";
		concepticonID = "";
		properties = new HashMap<>();
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public String getConcepticonID() {
		return concepticonID;
	}
	public void setConcepticonID(String concepticonID) {
		this.concepticonID = concepticonID;
	}
	public String getParamID() {
		return paramID;
	}
	public void setParamID(String paramID) {
		this.paramID = paramID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}