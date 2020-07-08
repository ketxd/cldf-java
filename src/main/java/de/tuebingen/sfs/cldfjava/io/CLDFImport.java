package de.tuebingen.sfs.cldfjava.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tuebingen.sfs.cldfjava.data.CLDFWordlistDatabase;
import de.tuebingen.sfs.cldfjava.data.*;

//import de.tuebingen.sfs.psl.engine.core.ModelStorePSL;

public class CLDFImport {

	public static CSVParser parser;
	public static CLDFWordlistDatabase database;
	/**
	 * TODO: This should build a CLDFDatabase object (see structure and interface there) from a directory with CLDF files.
	 *       The module we want to fully support is described here: https://github.com/cldf/cldf/tree/master/modules/Wordlist
	 *       Test cases are src/test/resources/lexirumah-2.0 and src/test/resources/northeuralex-0.9.
	 *       We should first read the metadata JSON file, and adapt the parser for the other files to the CSV dialect description (see https://github.com/cldf/cldf)
	 *       The functionality of importAtomsFromFile (and retrieveAtoms) should then be moved to LexicalAtomExtractor (see there)
	 * @param cldfDirName
	 * @return
	 */
	public static CLDFWordlistDatabase loadDatabase(String cldfDirName) {
		File path;
		File[] possibleJsons;
		File json;
		byte[] mapData;
		parser = new CSVParser();
		

		try {
			path = new File(cldfDirName);
			possibleJsons = path.listFiles((File dir, String name) ->  name.endsWith("json")); //possible json files in the given folder
			if(possibleJsons.length == 0) { //if 0, no json found in the folder
				throw new Error("Given folder does not contain json file!");
			} else {
				json = possibleJsons[0];
			}
			mapData = Files.readAllBytes(Paths.get(json.getAbsolutePath()));
			JsonNode root = new ObjectMapper().readTree(mapData);
			String moduleType = root.get("dc:conformsTo").asText().split("#")[1]; // extracting the module from the link

			if(moduleType.equals("Wordlist")) { //extracting the Wordlist module
				JsonNode tables = root.get("tables"); //extracting all tables of the module
				List<String> tableTypes = new ArrayList<>();

				for(JsonNode table : tables) {
					String tableType = table.get("dc:conformsTo").asText().split("#")[1]; //each table must conform to a type, which is added into the list
					tableTypes.add(tableType);
				}

				//index of a type of table in the list is the one we that we will refer to retrieve all values relevant for the specific table
				int formTableIndex = tableTypes.indexOf("FormTable");
				int languageTableIndex = tableTypes.indexOf("LanguageTable");
				int parameterTableIndex = tableTypes.indexOf("ParameterTable");
				int cognateTableIndex = tableTypes.indexOf("CognateTable");
				int cognateSetTableIndex = tableTypes.indexOf("CognatesetTable");

				//getting names of the files that stores tables
				String formFileName = tables.get(formTableIndex).get("url").asText();
				String languageFileName = tables.get(languageTableIndex).get("url").asText();
				String parameterFileName = tables.get(parameterTableIndex).get("url").asText();
				String cognateFileName = tables.get(cognateTableIndex).get("url").asText();

				//populating form, language and parameters maps (all have different methods because of different properties and object fields)
				Map<Integer, CLDFForm> idToForm = readFormCsv(path + "/" + formFileName, createColumnPropertyMap(formTableIndex, tables));
				Map<String, CLDFLanguage> langIDToLang = readLanguageCsv(path + "/" + languageFileName, createColumnPropertyMap(languageTableIndex, tables));
				Map<String, CLDFParameter> paramIDToParam = readParameterCsv(path + "/" + parameterFileName, createColumnPropertyMap(parameterTableIndex, tables));
				Map<Integer, CLDFCognateJudgement> cognateIDToCognate = readCognateCsv(path + "/" + cognateFileName, createColumnPropertyMap(cognateTableIndex, tables));
				
				//populating Cognateset map only happens if there is a separate file for that
				Map<Integer, CLDFCognateSet> cogSetIDToCogset = new HashMap<>();
				if(cognateSetTableIndex != -1) {
					String cognateSetFileName = tables.get(cognateSetTableIndex).get("url").asText();
					cogSetIDToCogset = readCognateSetCsv(path + "/" + cognateSetFileName, createColumnPropertyMap(cognateSetTableIndex, tables));
				}
				database = new CLDFWordlistDatabase(idToForm, langIDToLang, paramIDToParam, cognateIDToCognate, cogSetIDToCogset);
				database.currentPath = cldfDirName;

			} else {
				throw new Error("The json file doesn't contain the Wordlist module!");
			}

		} catch (NullPointerException e) {
			System.out.println("Error: Given folder does not exist!");
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return database;
	}

	/**
	 * A method to create the map from column names to their types
	 * @param tableIndex: an index of a table to retrieve values form
	 * @param tables: nodes of a table
	 * @return: map(property, column name)
	 */
	public static Map<String, String> createColumnPropertyMap(int tableIndex, JsonNode tables) {
		//columns might have different names, therefore we need a mapping from column name to its type
		Map<String, String> propertyToColumn = new HashMap<>();
		for(JsonNode column : tables.get(tableIndex).get("tableSchema").get("columns")) {
			//if no type is given, name of the column is considered the type
			String property =  column.get("propertyUrl") == null || !column.get("propertyUrl").asText().contains("#") ? column.get("name").asText() : column.get("propertyUrl").asText().split("#")[1];
			String columnName = column.get("name").asText();

			propertyToColumn.put(property, columnName);
		}
		return propertyToColumn;
	}

	/**
	 * A method for reading the Parameter Table
	 * @param path of the file to read
	 * @param propertyColumns a map of properties and their columns
	 * @return id to Parameter object map
	 */
	public static Map<String, CLDFParameter> readParameterCsv(String path, Map<String, String> propertyColumns) {
		BufferedReader bf = null;
		String line = "";
		Map<String, CLDFParameter> parameterTable = new HashMap<>();
		try {
			bf = new BufferedReader(new FileReader(path));
			List<String> columns = Arrays.asList(bf.readLine().split(","));  //all columns are split by comma
			
			//retrieving column indecies of each property, that will help us extract values and fill Object fields.
			//-1 needed when the field is not required, and therefore won't be extracted if not found
			int idIdx = columns.indexOf(propertyColumns.get("id"));
			int nameIdx = propertyColumns.containsKey("name") ? columns.indexOf(propertyColumns.get("name")) : -1;
			int concIdx = propertyColumns.containsKey("concepticonReference") ? columns.indexOf(propertyColumns.get("concepticonReference")) : -1;
			//in order to fill the "properties" map, for the columns that don't have a separate filed
			//make a list of column indecies that were used
			List<Integer> usedColumns = Arrays.asList(idIdx, nameIdx, concIdx);
			List<Integer> remainedColumns = new ArrayList<>();

			//fill a new list with unused columns with the remaining indecies
			for(int i=0; i<columns.size(); i++) {
				if(!usedColumns.contains(i)) {
					remainedColumns.add(i);
				}
			}

			while((line = bf.readLine()) != null) {
				//parsed column values of each row
				String[] column = parser.getColumns(line).toArray(new String[0]);
				CLDFParameter parameterEntry = new CLDFParameter();
				Map<String, String> properties = new HashMap<>();
				try {
					//if the amount of column names and the amount of retrieved column values are not the same, there must be some error in row formatting
					if(column.length != columns.size()) {
						throw new FormattingException(line, path);
					}
					//setting required fields
					parameterEntry.setParamID(column[idIdx]);

					//settings fields that aren't required by checking whether they exist
					if(nameIdx != -1) parameterEntry.setName(column[nameIdx]);
					if(concIdx != -1) parameterEntry.setConcepticonID(column[concIdx]);

					//for the indecies of remained columns, put them into a property map
					for(int j=0; j<remainedColumns.size(); j++) {
						String colVal = column[remainedColumns.get(j)];
						properties.put(columns.get(remainedColumns.get(j)), colVal);
					}

					parameterEntry.setProperties(properties);
					//mapping object and its id
					parameterTable.put(column[idIdx], parameterEntry);
				}    catch(FormattingException e) {
					
				}

			}
			bf.close();

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return parameterTable;
	}

	/**
	 * A method for reading the Language Table
	 * @param path of the file to read
	 * @param propertyColumns a map of properties and their columns
	 * @return id to Language object map
	 */
	public static Map<String, CLDFLanguage> readLanguageCsv(String path, Map<String, String> propertyColumns) {
		BufferedReader bf = null;
		Map<String, CLDFLanguage> languageTable = new HashMap<>();
		String line = "";

		try {
			bf = new BufferedReader(new FileReader(path));
			List<String> columns = Arrays.asList(bf.readLine().split(","));  //all columns are split by comma

			//retrieving column indecies of each property, that will help us extract values and fill Object fields.
			//-1 needed when the field is not required, and therefore won't be extracted if not found
			int idIdx = columns.indexOf(propertyColumns.get("id"));
			int isoIdx = propertyColumns.containsKey("iso639P3code") ? columns.indexOf(propertyColumns.get("iso639P3code")) : -1;
			int glottoIdx = propertyColumns.containsKey("glottocode") ? columns.indexOf(propertyColumns.get("glottocode")) : -1;
			int macroaIdx = propertyColumns.containsKey("macroarea") ? columns.indexOf(propertyColumns.get("macroarea")) : -1;
			int nameIdx = propertyColumns.containsKey("name") ? columns.indexOf(propertyColumns.get("name")) : -1;
			int familyIdx = propertyColumns.containsKey("Family") ? columns.indexOf(propertyColumns.get("Family")) : -1;
			int latitIdx = propertyColumns.containsKey("latitude") ? columns.indexOf(propertyColumns.get("latitude")) : -1;
			int longitIdx = propertyColumns.containsKey("longitude") ? columns.indexOf(propertyColumns.get("longitude")) : -1;
			//in order to fill the "properties" map, for the columns that don't have a separate filed
			//make a list of column indecies that were used
			List<Integer> usedColumns = Arrays.asList(idIdx, isoIdx, glottoIdx,macroaIdx , latitIdx, longitIdx, nameIdx);
			List<Integer> remainedColumns = new ArrayList<>();

			//fill a new list with unused columns with the remaining indecies
			for(int i=0; i<columns.size(); i++) {
				if(!usedColumns.contains(i)) {
					remainedColumns.add(i);
				}
			}

			while((line = bf.readLine()) != null) {
				//parsed column values of each row
				String[] column = parser.getColumns(line).toArray(new String[0]);
				CLDFLanguage languageEntry = new CLDFLanguage();
				Map<String, String> properties = new HashMap<>();
				try {
					//if the amount of column names and the amount of retrieved column values are not the same, there must be some error in row formatting
					if(column.length != columns.size()) {
						throw new FormattingException(line, path);
					}
					//setting required fields
					languageEntry.setLangID(column[idIdx]);

					//settings fields that aren't required by checking whether they exist
					if(isoIdx != -1) languageEntry.setIso(column[isoIdx]);
					if(glottoIdx != -1) languageEntry.setGlottocode(column[glottoIdx]);
					if(nameIdx != -1) languageEntry.setName(column[nameIdx]);
					if(familyIdx != -1) languageEntry.setFamily(column[familyIdx]);
					if(latitIdx != -1) languageEntry.setLatitude(column[latitIdx].isEmpty() ? Float.NaN : Float.parseFloat(column[latitIdx])); //if the value is empty, indication for a Float type
					if(longitIdx != -1) languageEntry.setLongitude(column[latitIdx].isEmpty() ? Float.NaN : Float.parseFloat(column[longitIdx])); //if the value is empty, indication for a Float type

					//for the indecies of remained columns, put them into a property map
					for(int j=0; j<remainedColumns.size(); j++) {
						String colVal = column[remainedColumns.get(j)];
						properties.put(columns.get(remainedColumns.get(j)), colVal);
					}

					languageEntry.setProperties(properties);
					//mapping object and its id
					languageTable.put(column[idIdx], languageEntry);
				}   catch(FormattingException e) {
					
				}
			}
			bf.close();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return languageTable;
	}

	/**
	 * A method for reading the Form Table
	 * @param path of the file to read
	 * @param propertyColumns a map of properties and their columns
	 * @return id to Form object map
	 */
	public static Map<Integer, CLDFForm> readFormCsv(String path, Map<String, String> propertyColumns) {
		BufferedReader bf = null;
		String line = "";
		Map<Integer, CLDFForm> formTable = new HashMap<>();

		try {
			bf = new BufferedReader(new FileReader(path));
			List<String> columns = Arrays.asList(bf.readLine().split(","));  //all columns are split by comma

			//value doesn't always have a specified property name, and is sometimes cold differently. Map possible names
			String value = propertyColumns.containsKey("Value") ? "Value" : "value";

			//retrieving column indecies of each property, that will help us extract values and fill Object fields.
			//-1 needed when the field is not required, and therefore won't be extracted if not found
			int idIdx = columns.indexOf(propertyColumns.get("id"));
			int langIdx = columns.indexOf(propertyColumns.get("languageReference"));
			int paramIdx = columns.indexOf(propertyColumns.get("parameterReference"));

			int formIdx = propertyColumns.containsKey("form") ? columns.indexOf(propertyColumns.get("form")) : -1;
			int valueIdx = propertyColumns.containsKey(value) ? columns.indexOf(propertyColumns.get(value)) : -1;
			int commentIdx = propertyColumns.containsKey("comment") ? columns.indexOf(propertyColumns.get("comment")) : -1;
			int segmentsIdx = propertyColumns.containsKey("segments") ? columns.indexOf(propertyColumns.get("segments")) : -1;

			//in order to fill the "properties" map, for the columns that don't have a separate filed
			//make a list of column indecies that were used
			List<Integer> usedColumns = Arrays.asList(idIdx, langIdx, paramIdx,formIdx , valueIdx, commentIdx, segmentsIdx);
			List<Integer> remainedColumns = new ArrayList<>();

			//fill a new list with unused columns with the remaining indecies
			for(int i=0; i<columns.size(); i++) {
				if(!usedColumns.contains(i)) {
					remainedColumns.add(i);
				}
			}

			while((line = bf.readLine()) != null) {
				//parsed column values of each row
				String[] column = parser.getColumns(line).toArray(new String[0]);
				CLDFForm formEntry = new CLDFForm();
				Map<String, String> properties = new HashMap<>();
				try {
					//if the amount of column names and the amount of retrieved column values are not the same, there must be some error in row formatting
					if(column.length != columns.size()) {
						throw new FormattingException(line, path);
					}
					//setting required fields
					formEntry.setId(Integer.parseInt(column[idIdx]));
					formEntry.setLangID(column[langIdx]);
					formEntry.setParamID(column[paramIdx]);

					//settings fields that aren't required by checking whether they exist
					if(formIdx != -1) formEntry.setForm(column[formIdx]);
					if(valueIdx != -1) formEntry.setOrigValue(column[valueIdx]);
					if(commentIdx != -1) formEntry.setComment(column[commentIdx]);
					if(segmentsIdx != 1) formEntry.setSegments(column[segmentsIdx].split(" "));

					//for the indecies of remained columns, put them into a property map
					for(int j=0; j<remainedColumns.size(); j++) {
						String colVal = column[remainedColumns.get(j)].isEmpty() ? "" : column[remainedColumns.get(j)];
						properties.put(columns.get(remainedColumns.get(j)), colVal);
					}

					formEntry.setProperties(properties);
					//mapping object and its id
					formTable.put(Integer.parseInt(column[idIdx]), formEntry);
				} catch(FormattingException e) {
					
				}
			}
			bf.close();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return formTable;
	}

	/**
	 * A method for reading the CoganteSet Table
	 * @param path of the file to read
	 * @param propertyColumns a map of properties and their columns
	 * @return id to Cognate object map
	 */
	public static Map<Integer, CLDFCognateJudgement> readCognateCsv(String path, Map<String, String> propertyColumns) {
		BufferedReader bf = null;
		String line = "";
		Map<Integer, CLDFCognateJudgement> cognateTable = new HashMap<>();

		try {
			bf = new BufferedReader(new FileReader(path));
			List<String> columns = Arrays.asList(bf.readLine().split(",")); //all columns are split by comma

			//retrieving column indecies of each property, that will help us extract values and fill Object fields. -1 needed when the field is not required, and therefore won't be extracted if not found
			int idIdx = columns.indexOf(propertyColumns.get("id"));
			int formIdx = columns.indexOf(propertyColumns.get("formReference"));
			int cogsetIdx = columns.indexOf(propertyColumns.get("cognatesetReference"));

			while((line = bf.readLine()) != null) {
				//parsed column values of each row
				String[] column = parser.getColumns(line).toArray(new String[0]);
				CLDFCognateJudgement cognateEntry = new CLDFCognateJudgement();
				//if the amount of column names and the amount of retrieved column values are not the same, there must be some error in row formatting
				try {
					if(column.length != columns.size()) {
						throw new FormattingException(line, path);
					}
					//setting required fields
					cognateEntry.setCognateID(Integer.parseInt(column[idIdx]));
					cognateEntry.setFormReference(Integer.parseInt(column[formIdx]));
					cognateEntry.setCognatesetReference(Integer.parseInt(column[cogsetIdx]));
					
					//mapping object and its id
					cognateTable.put(Integer.parseInt(column[idIdx]), cognateEntry);
				}  catch(FormattingException e) {
					
				}
			}
			bf.close();

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return cognateTable;
	}
	
	/**
	 * A method for reading the CognateSet Table
	 * @param path of the file to read
	 * @param propertyColumns a map of properties and their columns
	 * @return id to CognateSet object map
	 */
	public static Map<Integer, CLDFCognateSet> readCognateSetCsv(String path, Map<String, String> propertyColumns) {
		BufferedReader bf = null;
		String line = "";
		Map<Integer, CLDFCognateSet> cognatesetTable = new HashMap<>();

		try {
			bf = new BufferedReader(new FileReader(path));
			List<String> columns = Arrays.asList(bf.readLine().split(",")); //all columns are split by comma

			//retrieving column indecies of each property, that will help us extract values and fill Object fields. -1 needed when the field is not required, and therefore won't be extracted if not found
			int idIdx = columns.indexOf(propertyColumns.get("id"));
			int descriptionIdx = propertyColumns.containsKey("description") ? columns.indexOf(propertyColumns.get("description")) : -1;
			int sourceIdx = propertyColumns.containsKey("source") ? columns.indexOf(propertyColumns.get("source")) : -1;

			while((line = bf.readLine()) != null) {
				//parsed column values of each row
				String[] column = parser.getColumns(line).toArray(new String[0]);
				CLDFCognateSet cognateSetEntry = new CLDFCognateSet();
				//if the amount of column names and the amount of retrieved column values are not the same, there must be some error in row formatting
				try {
					if(column.length != columns.size()) {
						throw new FormattingException(line, path);
					}
					//setting required fields
					cognateSetEntry.setCogsetID(column[idIdx]);

					//settings fields that aren't required by checking whether they exist
					if(descriptionIdx != -1) cognateSetEntry.setDescription(column[descriptionIdx]);
					if(sourceIdx != -1) cognateSetEntry.setSources(Arrays.asList(column[sourceIdx].split(";")));
					
					//mapping object and its id
					cognatesetTable.put(Integer.parseInt(column[idIdx]), cognateSetEntry);
				}  catch(FormattingException e) {
					
				}
			}
			bf.close();

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return cognatesetTable;
	}

}

/**
 * @author kate
 * An exception class for detecting problems in csv file formatting
 */
class FormattingException extends Exception
{
	public FormattingException() {}

	public FormattingException(String line, String path)
	{
		
		System.err.println("ERROR in file " + path.substring(path.lastIndexOf("/")+1) +": The formatting of the row contains some errors!:");
		System.out.println(line);
	}
}
