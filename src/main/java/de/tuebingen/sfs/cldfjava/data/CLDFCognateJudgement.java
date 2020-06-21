package de.tuebingen.sfs.cldfjava.data;

public class CLDFCognateJudgement {
	int cognateID;
	int formReference;
	int cognatesetReference;
	
	public CLDFCognateJudgement() {
		cognateID = -1;
		formReference = -1;
		cognatesetReference = -1;
	}
	
	public int getCognateID() {
		return cognateID;
	}
	public void setCognateID(int cognateID) {
		this.cognateID = cognateID;
	}
	public int getFormReference() {
		return formReference;
	}
	public void setFormReference(int formReference) {
		this.formReference = formReference;
	}
	public int getCognatesetReference() {
		return cognatesetReference;
	}
	public void setCognatesetReference(int cognatesetReference) {
		this.cognatesetReference = cognatesetReference;
	}
}