package ca.diro.javadocindexer.servlets;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ca.diro.javadocindexer.Settings;

public class AddFieldStatusServlet extends HttpServlet {

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
		
		response.getWriter().append("{\"completionCourante\" : "+AddIndexNameServlet.completionCourante 
				+ ", \"completionTotal\": "+AddIndexNameServlet.completionTotal+"}");
		
		request.setAttribute("message", "message var");
		
	}
	
}
