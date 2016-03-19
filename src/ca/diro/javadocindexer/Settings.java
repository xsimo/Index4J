package ca.diro.javadocindexer;

import java.io.File;
import java.util.ArrayList;

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
		tmpDir = new File(TMP_DIR_PATH);
		tmpDir.mkdir();
		if(!tmpDir.isDirectory()) {
			throw new ExceptionInInitializerError(TMP_DIR_PATH + " is not a directory");
		}
		String realIndexPath = "";
		String realLibraryPath = "";
		if(realPath.contains(":")){
			INDEX_DIR_PATH = realIndexPath = "G:\\javadocIndexerLib\\index";
			staticHostURI = "http://localhost/";
			DESTINATION_DIR_PATH = realLibraryPath = "G:\\javadocIndexerLib\\library"; 
			dynamicHostURI = "http://localhost:8080";
		}else{
			INDEX_DIR_PATH = realIndexPath = "/opt/javadocIndexerLib";
			staticHostURI = "http://simonarame.com/";
			DESTINATION_DIR_PATH = realLibraryPath = "/var/www/html/public/simonarame.com/library";
			dynamicHostURI = "http://simonarame.com:8080";
		}
		
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
		File [] libraries = indexDir.listFiles();
		for(File lib : libraries){
			if(lib.isDirectory()){
				libraryList.add(lib.getName());
			}
		}
		WebContentDir = realPath;
	}
	
}
