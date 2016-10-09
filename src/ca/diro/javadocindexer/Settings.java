package ca.diro.javadocindexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.lang.ExceptionInInitializerError;

public final class Settings {

	public static final String sep = System.getProperty("file.separator");
	public static String staticHostURI;
	public static String dynamicHostURI;
	public static String WebContentDir;
	public static final String TMP_DIR_PATH = System.getProperty("java.io.tmpdir");
	public static File tmpDir;
	public static String DESTINATION_DIR_PATH;
	public static File destinationDir;
	public static String INDEX_DIR_PATH;
	public static File indexDir;
	public static ArrayList<String> libraryList;

	public Settings(String realPath) throws ExceptionInInitializerError{
		
		Properties properties = new Properties();
		File propertiesFile = new File(Thread.currentThread().getContextClassLoader()
				.getResource("Index4J.properties").getPath());
		
		try {
			properties.load(new FileInputStream(propertiesFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExceptionInInitializerError();
		}
		
		tmpDir = new File(TMP_DIR_PATH);
		tmpDir.mkdir();
		if(!tmpDir.isDirectory()) {
			throw new ExceptionInInitializerError(TMP_DIR_PATH + " is not a directory");
		}
		String realIndexPath = "";
		String realLibraryPath = "";

		INDEX_DIR_PATH = realIndexPath = (String) properties.get("realIndexPath");
		staticHostURI = (String) properties.get("staticHostURI");
		DESTINATION_DIR_PATH = realLibraryPath = (String) properties.get("realLibraryPath"); 
		dynamicHostURI = (String) properties.get("dynamicHostURI");
		
		
		destinationDir = new File(realLibraryPath);
		destinationDir.mkdir();
		System.out.println("destinationDir:"+destinationDir.getAbsolutePath());
		if(!destinationDir.isDirectory()) {
			throw new ExceptionInInitializerError(realLibraryPath+" is not a directory");
		}
		indexDir = new File(realIndexPath);
		indexDir.mkdir();
		System.out.println("indexDir:"+indexDir.getAbsolutePath());
		if(!indexDir.isDirectory()){
			throw new ExceptionInInitializerError("INDEX_DIR_PATH"+" is not a directory");
		}
		libraryList = new ArrayList<String>();
		Map<Long, String> lastModOrderedList = new TreeMap<Long, String>();
		
		File [] libraries = indexDir.listFiles();
		for(File lib : libraries){
			if(lib.isDirectory()){
				lastModOrderedList.put(lib.lastModified(), lib.getName());
			}
		}
		for(Map.Entry<Long,String> entry: lastModOrderedList.entrySet()){
			libraryList.add(entry.getValue());
		}
		WebContentDir = realPath;
	}
	
}
