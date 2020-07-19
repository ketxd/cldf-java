package de.tuebingen.sfs.cldfjava.data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a CLDF database in an object-oriented fashion. Created using CLDFImport.
 * 
 * @author jkaparina
 *
 */
public class CLDFWordlistDatabase {
	//maps as defined by the relevant parts of the CLDF specification (the wordlist module, plus the inherited more general structure)
	//generally, table rows are modeled as objects, whereas properties (single fields) are stored using elementary types
	Map<Integer, CLDFForm> idToForm; //contents of form table (using integer IDs from typically first column)
	Map<String, CLDFLanguage> langIDToLang; //from foreign key into language table
	Map<String, CLDFParameter> paramIDToParam; //from foreign key (concept ID) into parameters table (typically concepts.csv)

	//TODO: is it really needed?
	Map<Integer, CLDFCognateJudgement> cognateIDToCognate; //cognateID to cognate object
	Map<Integer, CLDFCognateSet> cogsetIDToCogset; //only fill this if in separate table, store within CLDFForm if it's just cognate set IDs
	public String currentPath;

	public CLDFWordlistDatabase() {
		this.langIDToLang=new HashMap<>();
		this.paramIDToParam=new HashMap<>();
		this.idToForm=new HashMap<>();
		this.cognateIDToCognate=new HashMap<>();
	}

	public CLDFWordlistDatabase(Map<Integer,CLDFForm> idToForm, Map<String,CLDFLanguage> langIDToLang, Map<String,CLDFParameter> paramIDToParam,
								Map<Integer,CLDFCognateJudgement> cognateIDToCognate, Map<Integer,CLDFCognateSet> cogsetIDToCogset) {
		this.idToForm = idToForm;
		this.langIDToLang = langIDToLang;
		this.paramIDToParam = paramIDToParam;
		this.cognateIDToCognate = cognateIDToCognate;
		this.cogsetIDToCogset = cogsetIDToCogset;
	}

	public void setCurrentPath(String path) {
		currentPath = path;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public Map<Integer, CLDFForm> getFormsMap() {
		return idToForm;
	}

	public Map<String, CLDFLanguage> getLanguageMap() {
		return langIDToLang;
	}

	public Map<String, CLDFParameter> getConceptMap() {
		return paramIDToParam;
	}


	public Map<Integer, Set<Integer>> getCogsetToCognates() {
		Map<Integer, Set<Integer>> cognateSets = new HashMap<>();

		for (Map.Entry<Integer, CLDFCognateJudgement> entry : cognateIDToCognate.entrySet()) {
			int cognateID = entry.getKey();
			int cogsetID = cognateIDToCognate.get(cognateID).getCognatesetReference();
			int formID = entry.getValue().getFormReference();

			cognateSets.putIfAbsent(cogsetID, new HashSet<>());
			cognateSets.get(cogsetID).add(formID);
		}

		return cognateSets;
	}

}