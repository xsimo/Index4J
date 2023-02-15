package ca.diro.javadocindexer.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LibraryServlet extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getAnonymousLogger();
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		String tube = request.getRequestURI();
		tube = tube.substring(request.getContextPath().length()+1);
		logger.log(Level.INFO,tube);
		request.setAttribute("tube", tube);
		String url="/viewDoc.jsp";
		ServletContext ctxt = getServletContext();
		RequestDispatcher rd = ctxt.getRequestDispatcher(url);
		request.setAttribute("backToSearchResults", URLEncoder.encode(request.getParameter("query"),"UTF-8"));
		rd.forward(request, response);
	}
	
}
