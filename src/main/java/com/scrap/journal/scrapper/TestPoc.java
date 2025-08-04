package com.scrap.journal.scrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestPoc {
	 public static void main(String[] args) {
		 loadConfigFiles();
	 }
	 
	 public static List<String> loadConfigFiles(){
			List<String> files = new ArrayList<>();
			Path directoryPath = Paths.get("src/main/resources/configfiles");

			try (Stream<Path> paths = Files.walk(directoryPath)) {
	        	files = paths
	                    .filter(Files::isRegularFile)
	                    .map(path -> directoryPath+path.getFileName().toString())
	                    .collect(Collectors.toList());

	        	files.forEach(System.out::println);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        System.out.println(files);
	        return files;
		}
}
