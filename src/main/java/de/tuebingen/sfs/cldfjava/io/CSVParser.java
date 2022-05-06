package de.tuebingen.sfs.cldfjava.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple CSV parser based on manual implementation of a regular grammar.
 */
public class CSVParser {
    BufferedReader in;
    List<String> columnTitles = null;
    String nextLine = null;

    public CSVParser(InputStream rawInputStream, boolean hasHeader) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(rawInputStream));
        if (hasHeader) {
            columnTitles = getColumns(in.readLine());
        }
        nextLine = in.readLine();
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("WARNING: unexpected IO exception when closing CSVParser!");
            e.printStackTrace();
        }
    }

    public String getColumnTitle(int colIdx) {
        if (columnTitles == null) {
            return "Column " + colIdx;
        }
        return columnTitles.get(colIdx);
    }

    public boolean hasNextRecord() {
        return (nextLine != null);
    }

    public List<String> getNextRecordAsList() throws IOException {
        List<String> nextRecord = getColumns(nextLine);
        nextLine = in.readLine();
        return nextRecord;
    }

    public Map<String,String> getNextRecordAsMap() throws IOException {
        List<String> fieldValues = getColumns(nextLine);
        nextLine = in.readLine();
        Map<String,String> nextRecord = new TreeMap<String,String>();
        for (int i = 0; i < fieldValues.size(); i++) {
            nextRecord.put(getColumnTitle(i), fieldValues.get(i));
        }
        return nextRecord;
    }

    public enum FSAState {NORMAL_MODE, IN_QUOTES, AFTER_QUOTES_IN_QUOTE;}
    /**
     * @param csvRow A row string from csv file
     * @return list of parsed column values
     */
    public static List<String> getColumns(String csvRow) {
        CharSequence row = csvRow;

        FSAState currentState = FSAState.NORMAL_MODE;
        List<String> columnContents = new ArrayList<>();

        StringBuilder currentColumn = new StringBuilder();
        for (int i = 0; i < row.length(); i++) {
            char currentChar = row.charAt(i);
            switch (currentChar) {
                case ',':
                    if (currentState == FSAState.NORMAL_MODE) {
                        columnContents.add(currentColumn.toString());
                        currentColumn = new StringBuilder();
                    } else if (currentState == FSAState.AFTER_QUOTES_IN_QUOTE) {
                        currentState = FSAState.NORMAL_MODE;
                        columnContents.add(currentColumn.toString());
                        currentColumn = new StringBuilder();
                    } else if (currentState == FSAState.IN_QUOTES) {
                        currentColumn.append(',');
                    }
                    break;
                case '"':
                    if (currentState == FSAState.NORMAL_MODE) {
                        currentState = FSAState.IN_QUOTES;
                        if (currentColumn.length() > 1) {
                            System.err.println("CSV parsing error: found unescaped quotation mark in middle of field value, assuming that a column separator is missing!");
                            System.err.println("    malformed row: " + row);
                            columnContents.add(currentColumn.toString());
                            currentColumn = new StringBuilder();
                        }
                    } else if (currentState == FSAState.IN_QUOTES) {
                        currentState = FSAState.AFTER_QUOTES_IN_QUOTE;
                    } else if (currentState == FSAState.AFTER_QUOTES_IN_QUOTE) {
                        currentColumn.append('\"');
                        currentState = FSAState.IN_QUOTES;
                    }
                    break;
                default:
                    if (currentState == FSAState.AFTER_QUOTES_IN_QUOTE) {
                        System.err.println("CSV parsing error: found single quotation mark in middle of quoted field value, assuming that it was intended to be escaped!");
                        System.err.println("    malformed row: " + row);
                        currentColumn.append('\"');
                        currentState = FSAState.IN_QUOTES;
                    }
                    currentColumn.append(currentChar);
            }
        }
         columnContents.add(currentColumn.toString());
        return columnContents;
    }

}

