package ca.diro.javadocindexer.servlets;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import ca.diro.javadocindexer.JavadocIndexerUtilities;
import ca.diro.javadocindexer.Settings;

public class AddIndexNameServlet extends HttpServlet {
	
	static Analyzer analyzer = JavadocIndexerUtilities.getAnalyzer();
	static double completionCourante;
	static double completionTotal;
	static long grandTotal;
	static long grandCompteur;
	
	static java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger("c.d.j.s."+AddIndexNameServlet.class.getName().substring(AddIndexNameServlet.class.getName().lastIndexOf(".")));
	
	public void init(ServletConfig config) throws ServletException, ExceptionInInitializerError {
		super.init(config);
		Settings instanceNotUsed = new Settings(getServletContext().getRealPath("/")); 
	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession();
		if(request.getUserPrincipal() == null){
			String url="/login_error.jsp";
			ServletContext ctxt = getServletContext();
			RequestDispatcher rd = ctxt.getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}
		String resultVar = "";
		completionTotal = 0;
		completionCourante = 0;
		grandTotal = 0;
		for(int i = 0;i<Settings.libraryList.size();i++) {
			String libName = Settings.libraryList.get(i);
			File libraryIndexDir = new File(Settings.INDEX_DIR_PATH+Settings.sep+libName);
			IndexReader reader = IndexReader.open(FSDirectory.open(libraryIndexDir));
			grandTotal += reader.maxDoc();
			reader.close();
		}
		grandCompteur = 0;
		for(int i = 0;i<Settings.libraryList.size();i++) {
			completionTotal = ((i+1.0)/Settings.libraryList.size())*100;
			logger.log(new LogRecord(Level.INFO, "Completion totale : " + i + " / " + Settings.libraryList.size()));
			String libName = Settings.libraryList.get(i);
			File libraryIndexDir = new File(Settings.INDEX_DIR_PATH+Settings.sep+libName);
			if(!libraryIndexDir.exists()){
				throw new IOException("libraryIndex directory does not exists : "+libraryIndexDir.getAbsolutePath());
			}
			Directory dir = FSDirectory.open(libraryIndexDir);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_34, analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			IndexWriter writer = new IndexWriter(dir, iwc);
			IndexReader reader = IndexReader.open(FSDirectory.open(libraryIndexDir));
			resultVar+=libName+" : "+reader.maxDoc()+"\n<br>\n";
			for(int j = 0; j<reader.maxDoc(); j++) {
				grandCompteur++;
				completionCourante = ((j+1.0)/reader.maxDoc())*100;
				if((j+1)%100==0)
				logger.log(new LogRecord(Level.INFO, grandCompteur + "/" + grandTotal));
				Document replacement = new Document();
				Document d = reader.document(j);
				for(Fieldable field : d.getFields()) {
					replacement.add(field);
				}
				Field f = new Field("libName", libName, Field.Store.YES, Field.Index.ANALYZED);
				f.setIndexOptions(IndexOptions.DOCS_ONLY);
				replacement.add(f);
				
				writer.updateDocument(new Term("path", d.getFieldable("path").stringValue()), replacement, analyzer);
				writer.commit();
			}
			writer.close(true);
			
		}
		request.setAttribute("result", resultVar);
		request.setAttribute("message", "message var");
		String url="/upload/Adjust.jsp";
		ServletContext ctxt = getServletContext();
		RequestDispatcher rd = ctxt.getRequestDispatcher(url);
		rd.forward(request, response);
	}
}
