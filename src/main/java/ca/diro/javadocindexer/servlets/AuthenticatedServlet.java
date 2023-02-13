package ca.diro.javadocindexer.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import ca.diro.javadocindexer.JobManager;

public class AuthenticatedServlet extends HttpServlet {

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if(config!=null && config.getServletContext() != null && config.getServletContext().getAttribute("jobManager")==null) {
			JobManager jobManager = new JobManager();
			config.getServletContext().setAttribute("jobManager", jobManager);
		}
	}
	
}
