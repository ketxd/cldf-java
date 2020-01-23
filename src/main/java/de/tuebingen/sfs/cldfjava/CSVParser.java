package de.tuebingen.sfs.cldfjava;

import java.util.List;
import java.nio.CharBuffer;
import java.util.ArrayList;

/**
 * A class to imitate FSA for CSV parsing
 */
public class CSVParser {
	public enum FSAState { NORMAL_MODE, QUOTES_MODE, DOUBLE_QUOTES_MODE;}

	public CSVParser() {}

	/**
	 * @param A row string from csv file
	 * @return list of parsed column values
	 */
	public List<String> getColumns(String csvRow) {
		CharSequence row = csvRow;

		FSAState currentState = FSAState.NORMAL_MODE;
		FSAState previousState = FSAState.NORMAL_MODE; // previous state is needed when we want to know whether previous entry was in a quoted state (hence already added), in this case we start a new entry instead of adding one 
		List<String> columns = new ArrayList<>();

		int begin = 0;
		int end = 0;
		boolean quotesBegin = true; //indicating beging or end of quotes
		
		for (int i=0; i<row.length(); i++) {
			char currentChar = row.charAt(i);
			switch(currentState) {
			case NORMAL_MODE:
				if(currentChar == ',') {
					if(!previousState.equals(FSAState.QUOTES_MODE)) {
						//if the previous state was Normal mode, we are saving the entry from whatever begin was, till the current char (first iteration begin is 0)
						end = i;
						columns.add(CharBuffer.wrap(row, begin, end).toString().trim());
						//new begin index is after the current comma
						begin = i + 1;
						//if begin index is the last char of the row and is also comma at the same time, it indicated the existence of an ampty string, so we add it
						if(begin == row.length()) columns.add("");
					} else {
						//the entry inside of quotes is already saved, so we just go further
						previousState = FSAState.NORMAL_MODE;
					}

				}
				if(begin < row.length() && i+1 == row.length()) columns.add(CharBuffer.wrap(row, begin, i+1).toString().trim()); //adding the very last entry that doesn't have comma at the end
				if(i+1 >= row.length()) break;
				if(row.charAt(i+1) == '\"') {
					//if next char is quotes, switch the mode
					currentState = FSAState.QUOTES_MODE;
				}
				break;

			case QUOTES_MODE:
				if(currentChar == '"') {
					if(quotesBegin) {
						quotesBegin = false;
						//changing the begin index to the char right after the quotes
						begin = i+1;
					} else {
						end = i;
						//if there are double quotes, we want to store them rather as single quotes, so replace
						//adding the whole entry inside of quotes
						columns.add(CharBuffer.wrap(row, begin, end).toString().replaceAll("\"\"", "\"").trim());
						//the new begin index must be a char following the closing quotes and a comma
						begin = i + 2;
						quotesBegin = true;
						//switching states
						currentState = FSAState.NORMAL_MODE;
						previousState = FSAState.QUOTES_MODE;
					}
				}
				if(i+3 >= row.length()) break;
				if(CharBuffer.wrap(row, i+1, i+3).toString().equals("\"\"")) {
					//if double quotes are present, go a level deeper, and skip double quotes
					currentState = FSAState.DOUBLE_QUOTES_MODE;
					i+=3;
				}
				break;
			case DOUBLE_QUOTES_MODE:
				if(i+3 >= row.length()) break;
				if(CharBuffer.wrap(row, i+1, i+3).toString().equals("\"\"")) {
					//if next double quortes are met, it means they are closing double quotes, go one level higher and skip quotes
					currentState = FSAState.QUOTES_MODE;
					i+=2;
				}
				break;
			}
		}
		return columns;
	}

}

