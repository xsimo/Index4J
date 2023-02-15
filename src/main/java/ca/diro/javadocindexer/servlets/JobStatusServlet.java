package ca.diro.javadocindexer.servlets;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class JobStatusServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		if(request.getUserPrincipal() == null){
			String url="/login_error.jsp";
			ServletContext ctxt = getServletContext();
			RequestDispatcher rd = ctxt.getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}
		
	}
	
}
