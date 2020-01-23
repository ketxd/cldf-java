package de.tuebingen.sfs.cldfjava.lexdata;

import java.util.ArrayList;
import java.util.List;

public class CLDFCognateSet {
	//TODO: use this to model cognate set objects as specified at
	// https://github.com/cldf/cldf/blob/master/components/cognatesets/CognatesetTable-metadata.json
	String cogsetID;
	String description;
	List<String> sources;
	//NOTE: not necessary to create these objects if no additional cognate set information beyond bare IDs is in the database
	
	public CLDFCognateSet() {
		cogsetID = "";
		description = "";
		sources = new ArrayList<>();
	}
	public String getCogsetID() {
		return cogsetID;
	}
	public void setCogsetID(String cogsetID) {
		this.cogsetID = cogsetID;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getSources() {
		return sources;
	}
	public void setSources(List<String> sources) {
		this.sources = sources;
	}
}