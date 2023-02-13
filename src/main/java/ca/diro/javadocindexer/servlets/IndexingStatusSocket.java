package ca.diro.javadocindexer.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import ca.diro.javadocindexer.IndexingJob;
import ca.diro.javadocindexer.JobManager;
/**
 * @inspired by tomcat.apache.org/tomcat-9.0/examples/ChatAnnotation.java
 * 
 */
@ServerEndpoint(value = "/upload/websocket/indexingStatus",  configurator=ServletAwareConfig.class)
public class IndexingStatusSocket{
	
	static java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger();
	private Session session;
	private EndpointConfig config;
	
	@OnOpen
    public void start(Session session, EndpointConfig config) {
		this.session = session;
        this.config = config;
        String message = "Websocket opened for live indexing status.\n";
        send(message);
        
        HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
        ServletContext servletContext = httpSession.getServletContext();
        String username = (String) config.getUserProperties().get("username");
        
        JobManager jobManager = ((JobManager)(servletContext.getAttribute("jobManager")));
        ArrayList<IndexingJob> subset = jobManager.getJobs(username);
        for(IndexingJob ij: subset) {
        	if(ij.iss == null || !ij.iss.isOpen()) {
        		ij.iss = this;
        	}
        }
	}
	
	@OnClose
    public void end() {
		String message = "goodbye.";
		if(this.isOpen()) {
			send(message);
		}
	}
	@OnMessage
    public void incoming(String message) {
		if(!message.equals("cancel")) {
			logger.log(Level.SEVERE, "Only accepting `cancel´ incoming message");
			return;
		}
		HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
        ServletContext servletContext = httpSession.getServletContext();
        String username = (String) config.getUserProperties().get("username");
        JobManager jobManager = ((JobManager)(servletContext.getAttribute("jobManager")));
        
        if(jobManager.jobCount(username)==0) {
        	close();
        }
	}
	
	@OnError
    public void onError(Throwable t) throws Throwable {
        logger.log(Level.SEVERE, "WebSocket Error: " + t.toString(), t);
    }
	
	public void send(String message) {
		try {
        	session.getBasicRemote().sendText(message);
        } catch (IOException e) {
        	logger.log(Level.SEVERE, "Error starting a session",e);
        	close();
        }
	}
	
	public void close() {
		try {
			session.close();
    	} catch (IOException e1) {
    		// Ignore
        }
	}
	
	public void conclude(long start) {
		long delta = System.currentTimeMillis()-start;
		send("\n\n"+"Indexing job ended ("+delta+" ms)");
	}
	
	public boolean isOpen() {
		return this.session.isOpen();
	}
}