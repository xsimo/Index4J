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
import java.util.logging.Level;
import java.util.logging.Logger;
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
	
	static Logger logger = Logger.getAnonymousLogger();

	public Settings(String realPath) throws ExceptionInInitializerError{
		
		Properties properties = new Properties();
		
		try {
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("Index4J.properties"));
			//org.apache.tomcat.util.file.ConfigFileLoader.getInputStream("index4jBase/JavadocIndex/WEB-INF/classes/Index4J.properties"));
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
		logger.log(Level.INFO,"destinationDir:"+destinationDir.getAbsolutePath());
		if(!destinationDir.isDirectory()) {
			throw new ExceptionInInitializerError(realLibraryPath+" is not a directory");
		}
		indexDir = new File(realIndexPath);
		indexDir.mkdir();
		logger.log(Level.INFO,"indexDir:"+indexDir.getAbsolutePath());
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
