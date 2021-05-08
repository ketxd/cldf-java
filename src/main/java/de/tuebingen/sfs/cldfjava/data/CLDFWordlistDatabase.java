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
	Map<Integer,String> originalFormIds;

	//TODO: is it really needed?
	Map<Integer, CLDFCognateJudgement> cognateIDToCognate; //cognateID to cognate object
	Map<String, CLDFCognateSet> cogsetIDToCogset; //only fill this if in separate table, store within CLDFForm if it's just cognate set IDs
	public String currentPath;
	List<String[]> exceptions;

	public CLDFWordlistDatabase() {
		this.langIDToLang=new HashMap<>();
		this.paramIDToParam=new HashMap<>();
		this.idToForm=new HashMap<>();
		this.cognateIDToCognate=new HashMap<>();
		this.originalFormIds=new HashMap<>();
	}

	public CLDFWordlistDatabase(Map<Integer,CLDFForm> idToForm, Map<String,CLDFLanguage> langIDToLang, Map<String,CLDFParameter> paramIDToParam,
								Map<Integer,CLDFCognateJudgement> cognateIDToCognate, Map<String,CLDFCognateSet> cogsetIDToCogset,Map<Integer,String> originalFormIds) {
		this.idToForm = idToForm;
		this.langIDToLang = langIDToLang;
		this.paramIDToParam = paramIDToParam;
		this.cognateIDToCognate = cognateIDToCognate;
		this.cogsetIDToCogset = cogsetIDToCogset;
		this.originalFormIds=originalFormIds;
	}

	public void setExceptions(List<String[]> exceptions) {
		this.exceptions=exceptions;
	}

	public List<String[]> getExceptions() {
		return this.exceptions;
	}

	public Map<Integer,String> getOriginalFormIds() {
		return this.originalFormIds;
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


	public Map<String, Set<Integer>> getCogsetToCognates() {
		Map<String, Set<Integer>> cognateSets = new HashMap<>();

		for (Map.Entry<Integer, CLDFCognateJudgement> entry : cognateIDToCognate.entrySet()) {
			int cognateID = entry.getKey();
			String cogsetID = cognateIDToCognate.get(cognateID).getCognatesetReference();
			int formID = entry.getValue().getFormReference();

			cognateSets.putIfAbsent(cogsetID, new HashSet<>());
			cognateSets.get(cogsetID).add(formID);
		}

		return cognateSets;
	}

	public List<String> listLanguageISOs() {
		ArrayList<String> isoCodes = new ArrayList<String>(langIDToLang.size());
		for (String langID : langIDToLang.keySet()) {
			isoCodes.add(langIDToLang.get(langID).getIso());
		}
		return isoCodes;
	}

	public List<Integer> listFormIdsForLangId(String langID) {
		List<Integer> formIDs = new LinkedList<Integer>();
		for (int formID : idToForm.keySet()) {
			if (langID.equals(idToForm.get(formID).getLangID()))
				formIDs.add(formID);
		}
		return formIDs;
	}

	public String searchLangIdForIsoCode(String isoCode) {
		for (CLDFLanguage lang : langIDToLang.values()) {
			if (isoCode.equals(lang.iso)) {
				return lang.langID;
			}
		}
		return null;
	}
}