package de.tuebingen.sfs.cldfjava.lexdata;

import java.util.HashMap;
import java.util.Map;

public class CLDFLanguage {
	//TODO: refactor ISO
	String langID; //the foreign key used by the rest of the database
	String iso639P3code;
	String glottocode;
	String name;
	String family;
	float latitude;
	float longitude;
	Map<String,String> properties; //to store additional info and remaining properties
	
	public CLDFLanguage() {
		langID = "";
		iso639P3code = "";
		glottocode = "";
		name = "";
		latitude = Float.NaN;
		longitude = Float.NaN;
		properties = new HashMap<>();
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public String getLangID() {
		return langID;
	}
	public void setLangID(String langID) {
		this.langID = langID;
	}
	public String getIso639P3code() {
		return iso639P3code;
	}
	public void setIso639P3code(String iso639p3code) {
		iso639P3code = iso639p3code;
	}
	public String getGlottocode() {
		return glottocode;
	}
	public void setGlottocode(String glottocode) {
		this.glottocode = glottocode;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	
	//TODO: write appropriate constructor, getters, treatment of gaps (default values)
}