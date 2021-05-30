package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class IniFile {
	private List<IniFileSection> sections = new ArrayList<>();
	
	public void addSection(IniFileSection section) {
		sections.add(section);
	}

	public Optional<IniFileSection> getSection(String sectionName) {
		return sections.stream()
				.filter(s -> s.getName().equals(sectionName))
				.findFirst();
	}

	public void read(BufferedReader br) throws IOException {
		String line;
		IniFileSection currentSection = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("[")) {
				String sectionName = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
				currentSection = new IniFileSection(sectionName);
				addSection(currentSection);
				List<String> sectionHeader = Arrays.asList(br.readLine().split(","));
				currentSection.setHeader(sectionHeader);
				continue;
			}
			currentSection.addRecord(line.split(","));
		}
	}

	public void write(BufferedWriter bw) throws IOException {
		for (IniFileSection section : sections) {
			section.write(bw);
		}
	} 
}
