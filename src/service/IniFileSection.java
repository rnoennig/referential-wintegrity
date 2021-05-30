package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IniFileSection {
	public static final String INI_FILE_SECTION_PRIMARY_KEYS = "primarykeys";
	public static final String INI_FILE_SECTION_FOREIGN_KEYS = "foreignkeys";
	public static final String INI_FILE_SECTION_COLUMNS = "columns";
	private String name;
	private List<String> header;
	private List<List<String>> records;

	public IniFileSection(String name) {
		this.name = name;
		records = new ArrayList<>();
	}

	public void addRecord(String... fields) {
		records.add(Arrays.asList(fields));
	}
	
	public void addRecord(List<String> fields) {
		records.add(fields);
	}

	public void write(BufferedWriter bw) throws IOException {
		bw.write('[');
		bw.write(name);
		bw.write(']');
		bw.newLine();
		
		writeFields(bw, header);
		
		for (List<String> record : records) {
			writeFields(bw, record);
		}
	}

	private void writeFields(BufferedWriter bw, List<String> values) throws IOException {
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				bw.write(',');
			}
			bw.write(values.get(i));
		}
		bw.newLine();
	}

	public void setHeader(String... fieldNames) {
		this.header = Arrays.asList(fieldNames);
	}
	
	public void setHeader(List<String> fieldNames) {
		this.header = fieldNames;
	}

	public String getName() {
		return name;
	}

	public List<List<String>> getRecords() {
		return records;
	}

	public int getIndex(String string) {
		return this.header.indexOf(string);
	}

	public List<String> getColumn(String fieldName) {
		return records.stream()
				.map(r -> r.get(getIndex(fieldName)))
				.collect(Collectors.toList());
	}

	public List<List<String>> getRecordsWithCondition(Predicate<? super List<String>> condition) {
		return records.stream().filter(condition).collect(Collectors.toList());
	}
}
