package de.tuebingen.sfs.cldfjava.data;

import de.tuebingen.sfs.cldfjava.data.*;

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
	Map<String, CLDFForm> idToForm; //contents of form table (using integer IDs from typically first column)
	Map<String, CLDFLanguage> langIDToLang; //from foreign key into language table
	Map<String, CLDFParameter> paramIDToParam; //from foreign key (concept ID) into parameters table (typically concepts.csv)

	Map<String, CLDFCognateJudgement> cognateIDToCognate; //cognateID to cognate object
	Map<String, CLDFCognateSet> cogsetIDToCogset; //only fill this if in separate table, store within CLDFForm if it's just cognate set IDs

	public CLDFWordlistDatabase(Map<String,CLDFForm> idToForm, Map<String,CLDFLanguage> langIDToLang, Map<String,CLDFParameter> paramIDToParam,
			Map<String,CLDFCognateJudgement> cognateIDToCognate, Map<String,CLDFCognateSet> cogsetIDToCogset) {
		this.idToForm = idToForm;
		this.langIDToLang = langIDToLang;
		this.paramIDToParam = paramIDToParam;
		this.cognateIDToCognate = cognateIDToCognate;
		this.cogsetIDToCogset = cogsetIDToCogset;
		fillAdditionalInfoOnForms();
	}

	public List<CLDFLanguage> getAllLanguages() {
		List<CLDFLanguage> languages = new ArrayList<>(langIDToLang.values());
		return languages;

	}

	public List<CLDFParameter> getAllConcepts() {
		List<CLDFParameter> concepts = new ArrayList<>(paramIDToParam.values());
		return concepts;

	}

	public List<CLDFForm> getAllForms() {
		List<CLDFForm> forms = new ArrayList<>(idToForm.values());
		return forms;

	}
	
	
	public void fillAdditionalInfoOnForms()  {

		for(CLDFForm form : idToForm.values()) {
			String iso = langIDToLang.values().stream().filter(o -> o.getLangID().equals(form.getLangID())).findFirst().get().getIso639P3code();
			String concepticon = paramIDToParam.values().stream().filter(o -> o.getParamID().equals(form.getParamID())).findFirst().get().getProperties().get("concepticon_proposed");
			if(concepticon == null) {
				concepticon = "";
			}
			form.setIso(iso);
			form.setConcepticon(concepticon);
			
		}
	}

	/**
	 * Lists all the language IDs used in the database.
	 * @return
	 */
	public List<String> listLanguageIDs() {
		List<String> languageIDs = new ArrayList<>(langIDToLang.keySet());
		return languageIDs;
	}

	/**
	 * Lists all the language ISO codes used in the database.
	 * @return
	 */
	public List<String> listLanguageISOs() {
		List<String> languageISOs = new ArrayList<>();
		for (CLDFLanguage lang : langIDToLang.values())
			languageISOs.add(lang.getIso639P3code());
		return languageISOs;
	}

	/**
	 * Lists all the parameter IDs used in the database.
	 * @return
	 */
	public List<String> listParameterIDs() {
		List<String> paramIDs = new ArrayList<>(paramIDToParam.keySet());
		return paramIDs;
	}

	/**
	 * Retrieves all the CLDFForm object for a given language-parameter pair.
	 * @return
	 */
	public List<CLDFForm> getForms(String langID, String paramID) {
		List<CLDFForm> forms = new ArrayList<>();

		idToForm.values().stream().filter(form -> form.getLangID().equals(langID) && form.getParamID().equals(paramID)).forEach(
				form -> {
					forms.add(form);
				}
				);
		return forms;
	}

	/**
	 * Retrieves all the CLDFForm objects for given languages.
	 * @return
	 */
	public List<CLDFForm> getFormsForLanguages(List<String> languageIDs) {
		List<CLDFForm> forms = new ArrayList<>();
		for(String language : languageIDs) {
			idToForm.values().stream().filter(form -> form.getLangID().equals(language)).forEach(
					form -> { 
						forms.add(form);
					});}
		return forms;
	}
	
	public List<CLDFForm> getFormsForLanguage(String languageID) {
		return idToForm.values().stream().filter(form -> form.getLangID().equals(languageID)).collect(Collectors.toList());
	}
	
	public List<CLDFForm> getFormsForConcept(String conceptID) {
		return idToForm.values().stream().filter(form -> form.getParamID().equals(conceptID)).collect(Collectors.toList());
	}

	/**
	 * Retrieves all the CLDFForm objects for given parameters.
	 * @return
	 */
	public List<CLDFForm> getFormsForParam(List<String> conceptIDs) {
		List<CLDFForm> forms = new ArrayList<>();
		for(String concept :conceptIDs) {
			idToForm.values().stream().filter(form -> form.getParamID().equals(concept)).forEach(
					form -> { 
						forms.add(form);
					});}
		return forms;
	}

	/**
	 * Retrieves all the CLDFLanguage objects for a given language id.
	 * @return
	 */
	public CLDFLanguage getLanguageObject(String langID) {
		return langIDToLang.getOrDefault(langID, null);
	}

	/**
	 * Retrieves all the CLDFLanguage objects for a given language id.
	 * @return
	 */
	public List<CLDFLanguage> getLanguageObjects(String langID) {
		List<CLDFLanguage> languages = new ArrayList<>();

		langIDToLang.values().stream().filter(lang -> lang.getLangID().equals(langID)).forEach(
				lang -> {
					languages.add(lang);
				}
				);
		return languages;
	}

	/**
	 * Retrieves all the CLDFLanguage objects for a given language id.
	 * @return
	 */
	public List<CLDFParameter> getParameterObjects(String paramID) {
		List<CLDFParameter> parameters = new ArrayList<>();

		paramIDToParam.values().stream().filter(param -> param.getParamID().equals(paramID)).forEach(
				param -> {
					parameters.add(param);
				}
				);
		return parameters;
	}




	public Map<String, CLDFCognateSet> getCognateToCogset() {
		Map<String, CLDFCognateSet> cognateIDToCogset = new HashMap<>();

		for(String ref : cognateIDToCognate.keySet()) {
			if(cogsetIDToCogset.containsKey(cognateIDToCognate.get(ref).getCognatesetReference())) {
				cognateIDToCogset.put(ref, cogsetIDToCogset.get(cognateIDToCognate.get(ref).getCognatesetReference()));
			}		
		}
		return cognateIDToCogset;
	}

	public Map<String, CLDFCognateJudgement> getCognateIDToCognate() {
		return cognateIDToCognate;
	}

	public void setFormCogsets() {
		for (Map.Entry<String, CLDFCognateJudgement> entry : cognateIDToCognate.entrySet()) {
			String cognateID = entry.getKey();
			String cogsetID = cognateIDToCognate.get(cognateID).getCognatesetReference();
			String formID = entry.getValue().getFormReference();
			CLDFForm form = idToForm.get(formID);
			form.setCogsetID(cogsetID);
		}
	}


	public Map<String, Set<CLDFForm>> getCogsetToCognates() {
		Map<String, Set<CLDFForm>> cognateSets = new HashMap<>();

		for (Map.Entry<String, CLDFCognateJudgement> entry : cognateIDToCognate.entrySet()) {
			String cognateID = entry.getKey();
			String cogsetID = cognateIDToCognate.get(cognateID).getCognatesetReference();
			String formID = entry.getValue().getFormReference();
			CLDFForm form = idToForm.get(formID);
			if (!cognateSets.containsKey(cogsetID))
				cognateSets.put(cogsetID, new HashSet<>());
			cognateSets.get(cogsetID).add(form);
		}

		return cognateSets;
	}

	//TODO: add all kinds of additional getters here, but pragmatically depending on actual need in the user interface
}