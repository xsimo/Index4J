package ca.diro.javadocindexer;

import java.io.File;
import java.util.UUID;

import ca.diro.javadocindexer.servlets.IndexingStatusSocket;

public class IndexingJob implements Runnable {

	File libraryIndexDir, libraryDir;
	String contextPath, libraryName;
	UUID id;
	StringBuffer sb;
	
	public IndexingStatusSocket iss;
	
	public long start;
	
	private boolean hasEnded;
	
	public IndexingJob(File libraryIndexDir, File libraryDir, String contextPath, String libraryName) throws Exception {
		this.libraryIndexDir = libraryIndexDir;
		this.libraryDir = libraryDir;
		this.contextPath = contextPath;
		this.id = java.util.UUID.randomUUID();
		this.sb = new StringBuffer();
		this.hasEnded = false;
		this.libraryName = libraryName;
	}
	
	@Override
	public void run() {
		start = System.currentTimeMillis();
		try {
			Indexer.index(libraryIndexDir,libraryDir,contextPath, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getId() {
		return id.toString();
	}
	
	public void feedback(String s) {
		iss.send(s);
	}
	
	public void end() {
		this.hasEnded= true;
		if(this.iss.isOpen()) {
			this.iss.conclude(start);
		}
	}
	
	public boolean isEnded() {
		return this.hasEnded;
	}
	
}
