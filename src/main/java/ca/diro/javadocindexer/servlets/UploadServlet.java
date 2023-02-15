package ca.diro.javadocindexer.servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;

import ca.diro.javadocindexer.Indexer;
import ca.diro.javadocindexer.IndexingJob;
import ca.diro.javadocindexer.JobManager;
import ca.diro.javadocindexer.Settings;
import ca.diro.javadocindexer.Unzip;


/**
 * apart from its main name utility
 * this class also serves as a static values holder 
 * 
 * @author Simon Arame xsimo.ca opensource copyright 2016
 *
 */
public class UploadServlet extends AuthenticatedServlet {
	
	private static final int libraryNameMaxLength = 50;
	static Logger logger = Logger.getAnonymousLogger();
	
	public void init(ServletConfig config) throws ServletException, ExceptionInInitializerError {
		super.init(config);
		Settings instanceNotUsed = new Settings(getServletContext().getRealPath("/")); 
	}
	
	/**
	 * I recommend using 7-zip to zip a folder containing some javadoc
	 * The library used here to decompress archive does not recognize
	 * the files compressed with the windows right click utility
	 * (not tested with winrar nor winzip nor other compression 
	 * tools not aboved mentionned.
	 * 
	 * doPost method is inspired by tutorial at page http://www.servletworld.com/servlet-tutorials/servlet-file-upload-example.html
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 *  
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		if(request.getUserPrincipal() == null){
			String url="/login_error.jsp";
			ServletContext ctxt = getServletContext();
			RequestDispatcher rd = ctxt.getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}
		String out = "";
		String message ="";
		DiskFileItemFactory  fileItemFactory = new DiskFileItemFactory ();
		/*
		 *Set the size threshold, above which content will be stored on disk.
		 */
		fileItemFactory.setSizeThreshold(1*1024*1024); //1 MB
		/*
		 * Set the temporary directory to store the uploaded files of size above threshold.
		 */
		fileItemFactory.setRepository(Settings.tmpDir);
 
		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		try {
			
			/*
			 * Parse the request
			 */
			String libraryName = null; 
			File file = null;
			String filename = null;
			//Normally only one zip file should be provided using the upload post method
			
			List list = uploadHandler.parseRequest(request);
			Iterator itr = list.iterator();
			while(itr.hasNext()){
				FileItem item = (FileItem) itr.next();
				if(item.getFieldName().equals("libraryName")){
					libraryName = item.getString();
				}
			}
			if(!StringUtils.isAsciiPrintable(libraryName) || libraryName.length()>libraryNameMaxLength){
				String url="/upload/Upload.jsp";
				if(libraryName.length()>libraryNameMaxLength){
					request.setAttribute("message", "Le nom de librairie ne doit pas d&eacute;passer "+libraryNameMaxLength+" caract&egrave;res");
				}else{
					request.setAttribute("message", "Le nom de librairie ne doit pas contenir de caract&egrave;res accentu&eacute;s");
				}
				ServletContext ctxt = getServletContext();
				RequestDispatcher rd = ctxt.getRequestDispatcher(url);
				rd.forward(request, response);
				return;
			}
			File libraryDir = new File(Settings.destinationDir,libraryName);
			libraryDir.mkdir();
			Iterator itr2 = list.iterator();
			while(itr2.hasNext()){
				FileItem fileItem = (FileItem) itr2.next();
				if(fileItem.getFieldName().equals("file1")){
					file = new File(libraryDir,libraryDir.getName()+".zip");
					fileItem.write(file);
				}
			}
			
			if(!Settings.libraryList.contains(libraryName)){
				Settings.libraryList.add(libraryName);
			}
			Unzip.main(file.getAbsolutePath(),libraryDir.getAbsolutePath());
			File libraryIndexDir = new File(Settings.INDEX_DIR_PATH+Settings.sep+libraryName);
			libraryIndexDir.mkdir();
			logger.log(Level.INFO,libraryIndexDir.getAbsolutePath());
			
			IndexingJob job = new IndexingJob(libraryIndexDir, libraryDir, request.getContextPath(), libraryName);
			Thread t = new Thread(job);
			t.start();
			((JobManager)(getServletContext().getAttribute("jobManager"))).addJob(request.getUserPrincipal().getName(), job);
			
			
			message = "Le fichier "+libraryName+" est en cours d'ajout (" + job.getId() + ")"
			+"<span style='display:none;' id='contextPathValue' >"+request.getContextPath()+"</span>"
					+"<script type=\"text/javascript\" src=\""+request.getContextPath()+"/upload/Uplink.js\"></script>";
			
		}catch(FileUploadException fue){
			message+= "error while uploading file 1 : " + fue.getMessage();
			fue.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
			message+= "error while uploading file 2 : " + e.getMessage()+e.getClass();
		}
		request.setAttribute("result", out);
		request.setAttribute("message", message);
		String url="/upload/Upload.jsp";
		ServletContext ctxt = getServletContext();
		RequestDispatcher rd = ctxt.getRequestDispatcher(url);
		rd.forward(request, response);
	}
}
