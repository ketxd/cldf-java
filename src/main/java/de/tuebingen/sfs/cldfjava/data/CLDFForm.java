package de.tuebingen.sfs.cldfjava.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CLDFForm {
	int id; //the id used to reference forms in other tables (e.g. cognates.csv or borrowings.csv)
	String langID; //Language_ID
	String paramID; //Parameter_ID
	String form;
	List<String> segments;
	String origValue; //Value
	String comment;
//	int cogsetID;
	Map<String,String> properties; //to store additional info, e.g. the original orthography, under their column name
	String[] alignedSegments;
	String iso;
	String concepticon;

	public CLDFForm() {
		id = -1;
		langID = "";
		paramID = "";
		form = "";
		origValue = "";
		comment = "";
//		cogsetID = -1;
		segments = new ArrayList<>();
		properties = new HashMap<>();
		alignedSegments = new String[]{};
		iso = "";
		concepticon = "";
	}

	public String getIso() {
		return iso;
	}

	public void setIso(String iso) {
		this.iso = iso;
	}

	public String getConcepticon() {
		return concepticon;
	}

	public void setConcepticon(String concepticon) {
		this.concepticon = concepticon;
	}
	
	public String[] getAlignedSegments() {
		return alignedSegments;
	}

	public void setAlignedSegments(String[] alignedSegments) {
		this.alignedSegments = alignedSegments;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
//	public int getCogsetID() {
//		return cogsetID;
//	}
//	public void setCogsetID(int cogsetID) {
//		this.cogsetID = cogsetID;
//	}
	public String getForm() {
		return form;
	}
	public void setForm(String form) {
		this.form = form;
	}
	public List<String> getSegments() {
		return segments;
	}
	public void setSegments(List<String> segments) {
		this.segments = segments;
	}
	public String getOrigValue() {
		return origValue;
	}
	public void setOrigValue(String origValue) {
		this.origValue = origValue;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getLangID() {
		return langID;
	}
	public void setLangID(String langID) {
		this.langID = langID;
	}
	public String getParamID() {
		return paramID;
	}
	public void setParamID(String paramID) {
		this.paramID = paramID;
	}

	public String toString() {
		return id + "\t" + form + "\t" + langID + "\t" + paramID;
	}
	

}