package ca.diro.javadocindexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Indexer {	
	
	public enum DocType {CLASSE,AUTRES};
	public static boolean once;
	static Analyzer analyzer = JavadocIndexerUtilities.getAnalyzer();
	static String libraryDirPath;
	
	static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Indexer.class.getName());
	
	public static void index(File libraryIndexDir, File libraryDir, String contextPath, IndexingJob job) throws Exception {
		libraryDirPath = libraryDir.getAbsolutePath();
		
		if(!libraryIndexDir.exists()){
			throw new Exception("libraryIndex directory does not exists : "+libraryIndexDir.getAbsolutePath());
		}
		once = false;
		Directory dir = FSDirectory.open(libraryIndexDir);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_34, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, iwc);
		index(writer, libraryDir, job);
		writer.close();
		job.end();
		return;
	}
	static void index(IndexWriter writer, File file, IndexingJob job){
		String out = "";
		String modified28OctobreByeBye = "null";
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						index(writer,new File(file, files[i]), job);
					}
				}
			} else {
				
				if(!file.getName().endsWith(".html")){
					return;
				}
				
				//Determination du type de document grace a cyberneko
				DOMParser parser = new DOMParser();
				try{
					parser.parse(new InputSource(new FileInputStream(file.getPath())));
				}catch(SAXException se){
					logger.log(Level.WARNING, se.getMessage(), se);
					job.feedback("Erreur 1 : "+se.getMessage());
				}catch(IOException ioe){
					logger.log(Level.WARNING, ioe.getMessage(), ioe);
					job.feedback("Erreur 2 : "+ioe.getMessage());
				}
				Node node = parser.getDocument();
				DocType currentDocType = JavadocIndexerUtilities.getDocType(node);
				
				//s'il ne s'agit pas d'une classe
				if(currentDocType!=DocType.CLASSE){
					//C'est peut-etre un sommaire de paquet auquel cas on traite le document
					indexPackageSummary(file, out, writer, job);
				}else{
					indexClass(file,out,writer, job);
				}
			}
		}else{
			job.feedback("\n Error : can not read "+file.getAbsolutePath());
		}
		return;
	}
	static void indexClass(File file,String out,IndexWriter writer, IndexingJob job){
		// make a new, empty document
		Document doc = new Document();

		//Path Field
		Field pathField = new Field("path", file.getPath().substring(libraryDirPath.length()), Field.Store.YES, Field.Index.ANALYZED);
		pathField.setIndexOptions(IndexOptions.DOCS_ONLY);
		doc.add(pathField);
		
		String title = file.getName().substring(0,file.getName().indexOf(".html"));
		job.feedback("\n"+title+" ");
		
		//Title field
		Field classTitleField = new Field("classTitle",title,Field.Store.YES, Field.Index.ANALYZED);
		classTitleField.setBoost(33.0f);
		classTitleField.setIndexOptions(IndexOptions.DOCS_ONLY);
		doc.add(classTitleField);

		//Modified Field
		NumericField modifiedField = new NumericField("modified");
		modifiedField.setLongValue(file.lastModified());
		doc.add(modifiedField);

		//Content field
		String content = "";
		String line = "";
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(file,"r");
			while((line= raf.readLine())!=null)content +=line;
		}catch(IOException ioe){
			logger.log(Level.WARNING, ioe.getMessage(), ioe);
			job.feedback("Erreur 3 : "+ioe.getMessage());
		}finally{
			try{raf.close();}catch(IOException ioe1){;}
		}
		String classContent = content.substring(
				content.indexOf("<!-- ======== START OF CLASS DATA ======== -->"),
				content.indexOf("<!-- ========= END OF CLASS DATA ========= -->"));
		Source source = new Source(classContent);
		net.htmlparser.jericho.TextExtractor te = new TextExtractor(source);
		source.setLogger(null);
		Field contentField = new Field("contents",te.toString(),Field.Store.YES,Field.Index.ANALYZED);
		contentField.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		doc.add(contentField);
		
		//Adding the methods one by one
		int begin = content.indexOf("<!-- ============ METHOD DETAIL ========== -->");
		int end = content.indexOf("<!-- ========= END OF CLASS DATA ========= -->");
		if(begin != -1 && end != -1){
			String allMethodDetails = content.substring(begin,end);
			int i = StringUtils.indexOfIgnoreCase(allMethodDetails,"<A NAME=",0);
			int j = StringUtils.indexOfIgnoreCase(allMethodDetails,"<A NAME=",i+8);
			if(i!=-1 && j!=-1){
				boolean once = true;
				do {
					i=Integer.valueOf(j);
					j = StringUtils.indexOfIgnoreCase(allMethodDetails,"<A NAME=",i+8);
					if(j==-1){
						once = false;
						j = allMethodDetails.length()-1;
					}
					String oneMethod = allMethodDetails.substring(i,j);
					int methodNameBegin = StringUtils.indexOfIgnoreCase(oneMethod,"<H3>");
					int methodNameEnd = StringUtils.indexOfIgnoreCase(oneMethod,"</H3");
					//If the documentation is made with the latest Javadoc Version, then the methods anchor are surrounded by <h4> not <H3>
					if(methodNameBegin == -1 || methodNameEnd==-1){
						methodNameBegin = StringUtils.indexOfIgnoreCase(oneMethod,"<h4>");
						methodNameEnd = StringUtils.indexOfIgnoreCase(oneMethod,"</h4");
					}
					if(methodNameBegin != -1 && methodNameEnd !=-1){
						String oneMethodName = oneMethod.substring(methodNameBegin+4,methodNameEnd);
						Field methodField = new Field("methodNames",oneMethodName,Field.Store.YES,Field.Index.ANALYZED);
						methodField.setBoost(16.0f);
						methodField.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
						doc.add(methodField);
						
						//Adding the return types
						
						int boundedToSignatureEnd = StringUtils.indexOfIgnoreCase(oneMethod,"<div");
						if(boundedToSignatureEnd==-1){
							boundedToSignatureEnd = StringUtils.indexOfIgnoreCase(oneMethod, "<dl>");
							if(boundedToSignatureEnd==-1){
								boundedToSignatureEnd = StringUtils.indexOfIgnoreCase(oneMethod, "</li>");
							}
						}
						String boundedToSignature = oneMethod.substring(0,boundedToSignatureEnd);
						
						int preBegin = StringUtils.indexOfIgnoreCase(boundedToSignature,"<pre>");
						int preEnd = StringUtils.indexOfIgnoreCase(boundedToSignature, "</pre>");
						if(preBegin!=-1 && preEnd !=-1){
							String narrow = boundedToSignature.substring(preBegin,preEnd);
							
							//first check if return type is void
							Pattern voidPattern = Pattern.compile("<pre>[^(]*void[^(]+");
							Matcher mVoid = voidPattern.matcher(narrow);
							if(!mVoid.find()){
								String payload ="";
								Pattern pattern = Pattern.compile("<a href=\"[^\"]+\" title=\"[^\"]+\">([^<]+)</a>");
								Matcher m = pattern.matcher(narrow);
								if(m.find()){
										payload = m.group(1);
								}
								while(payload.indexOf("@")!=-1){
									narrow = narrow.substring(m.group(0).length());
									pattern = Pattern.compile("<a href=\"[^\"]+\" title=\"[^\"]+\">([^<]+)</a>");
									m = pattern.matcher(narrow);
									if(m.find()){
										payload = m.group(1);
									}else{
										payload = "";
									}
								}
								if(!payload.equals("")){
									Field returnTypeField = new Field("returnType",payload,Field.Store.YES,Field.Index.ANALYZED);
									returnTypeField.setBoost(16.0f);
									doc.add(returnTypeField);
								}
							}else{
								String payload = "void";
								Field returnTypeField = new Field("returnType",payload,Field.Store.YES,Field.Index.ANALYZED);
								returnTypeField.setBoost(16.0f);
								doc.add(returnTypeField);
							}
						}
					}else{
						//But If (as in Java 6's Comparable.html), a method have more than one name anchor before its title so here we continue
					}
				}while(once);
			}
		}
		
		//Package field
		begin = content.indexOf("<!-- ======== START OF CLASS DATA ======== -->");
		end = content.indexOf("</FONT>",begin);
		if(begin != -1 && end != -1){
			String packageString = content.substring(begin,end);
			int i = packageString.indexOf("<FONT SIZE=\"-1\">")+16;
			String packageName = packageString.substring(i);
			Field packageField = new Field("packageName",packageName,Field.Store.YES,Field.Index.ANALYZED);
			packageField.setBoost(16.0f);
			packageField.setIndexOptions(IndexOptions.DOCS_ONLY);
			packageField.setBoost(16.0f);
			doc.add(packageField);
		}
		
		//CamelCase Field
		String[] parts = StringUtils.splitByCharacterTypeCamelCase(title);
		for(String part : parts){
			Field camelCaseField = new Field("camelCase",part,Field.Store.YES,Field.Index.ANALYZED);
			camelCaseField.setIndexOptions(IndexOptions.DOCS_ONLY);
			doc.add(camelCaseField);
		}
		try{
			job.feedback("\nupdating " + file);
			logger.log(Level.INFO, "updating "+file);
			
			writer.updateDocument(new Term("path", pathField.stringValue()), doc, analyzer);
		}catch(IOException ioe){
			logger.log(Level.WARNING, ioe.getMessage(), ioe);
			job.feedback("Erreur 4 : "+ioe.getMessage());
		}
		return;
	}
	static void indexPackageSummary(File file, String out,IndexWriter writer, IndexingJob job){
		String title = file.getName().substring(0,file.getName().indexOf(".html"));
		if(title.equals("package-summary")){
			Document doc = new Document();
			//Path field for Package-Summary 
			Field pathField = new Field("path", file.getPath().substring(libraryDirPath.length()), Field.Store.YES, Field.Index.ANALYZED);
			pathField.setIndexOptions(IndexOptions.DOCS_ONLY);
			doc.add(pathField);
			
			//"modified" field for Package-Summary
			NumericField modifiedField = new NumericField("modified");
			modifiedField.setLongValue(file.lastModified());
			doc.add(modifiedField);
			
			//"packageName" field for Package-Summary
			RandomAccessFile raf = null;
			String content = "";
			try {
				raf = new RandomAccessFile(file,"r");
				String line = "";
				while((line= raf.readLine())!=null)content +=line;
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, e.getMessage(),e);
				job.feedback("ok3"+e.getMessage());
				return;
			} catch (IOException ioe) {
				logger.log(Level.SEVERE, ioe.getMessage(), ioe);
				job.feedback("ok4"+ioe.getMessage());
				return;
			} finally{
				try{raf.close();}catch(IOException ioe1){;}
			}
			int begin = content.indexOf("<!-- ========= END OF TOP NAVBAR ========= -->");
			if(begin==-1)begin = content.indexOf("<!-- =========== END OF NAVBAR =========== -->");
			String packageContent = null;
			if(begin!=-1){
				int end = 0;
				String packageString = "";
				String packageName = "";
				try{
					 end = content.indexOf("</H2>",begin);
					 //the longest package name of Java 7 has 41 chars 
					 if(end-begin>200){
						 throw new StringIndexOutOfBoundsException();
					 }
					 packageString = content.substring(begin,end);
					 packageName = packageString.substring(packageString.indexOf("<H2>")+4);
				}catch(StringIndexOutOfBoundsException sioobe){
					end = content.indexOf("</h1>",begin);
					packageString = content.substring(begin,end);
					packageName = packageString.substring(packageString.indexOf("class=\"title\">")+14);
				}
				packageName = packageName.substring(8);
				Field packageField = new Field("packageName",packageName,Field.Store.YES, Field.Index.ANALYZED);
				packageField.setBoost(33.0f);
				packageField.setIndexOptions(IndexOptions.DOCS_ONLY);
				doc.add(packageField);
								
				int packageStart = content.indexOf("<!-- ========= END OF TOP NAVBAR ========= -->");
				if(packageStart==-1){
					packageStart = content.indexOf("<!-- =========== END OF NAVBAR =========== -->");
				}
				int packageEnd = content.indexOf("<!-- ======= START OF BOTTOM NAVBAR ====== -->");
				if(packageEnd==-1){
					packageEnd = content.indexOf("<!-- ========== START OF NAVBAR ========== -->",packageStart);
				}
				packageContent = content.substring(packageStart,packageEnd);
			}else{
				packageContent = content;
			}
			
			//"contents" field for Package-Summary
			Source source = new Source(packageContent);
			net.htmlparser.jericho.TextExtractor te = new TextExtractor(source);
			source.setLogger(null);
			Field contentField = new Field("contents",te.toString(),Field.Store.YES,Field.Index.ANALYZED);
			contentField.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
			doc.add(contentField);
			
			//Ecriture du document dans l'index
			job.feedback("\nupdating " + file);
			logger.log(Level.INFO, "updating "+file);
			try{
				
				writer.updateDocument(new Term("path", pathField.stringValue()), doc, analyzer);
			}catch(IOException ioe){
				logger.log(Level.SEVERE, ioe.getMessage(), ioe);
				job.feedback("Erreur 5 : "+ioe.getMessage());
			}
			
		}
		return;
	}
	
}
