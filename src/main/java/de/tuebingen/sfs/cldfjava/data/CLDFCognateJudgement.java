package de.tuebingen.sfs.cldfjava.data;

public class CLDFCognateJudgement {
	String cognateID;
	String formReference;
	String cognatesetReference;
	
	public CLDFCognateJudgement() {
		cognateID = "";
		formReference = "";
		cognatesetReference = "";
	}
	
	public String getCognateID() {
		return cognateID;
	}
	public void setCognateID(String cognateID) {
		this.cognateID = cognateID;
	}
	public String getFormReference() {
		return formReference;
	}
	public void setFormReference(String formReference) {
		this.formReference = formReference;
	}
	public String getCognatesetReference() {
		return cognatesetReference;
	}
	public void setCognatesetReference(String cognatesetReference) {
		this.cognatesetReference = cognatesetReference;
	}
}