package de.tuebingen.sfs.cldfjava.data;

public class CLDFCognateJudgement {
    int cognateID;
    int formReference;
    String cognatesetReference;

    public CLDFCognateJudgement() {
        cognateID = -1;
        formReference = -1;
        cognatesetReference = "";
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

    public String getCognatesetReference() {
        return cognatesetReference;
    }

    public void setCognatesetReference(String cognatesetReference) {
        this.cognatesetReference = cognatesetReference;
    }
}