package ca.diro.javadocindexer;

import java.util.ArrayList;
import java.util.HashMap;

public class JobManager {
	HashMap<String, ArrayList<IndexingJob>> jobs;
	public JobManager() {
		this.jobs = new HashMap<>();
	}
	public ArrayList<IndexingJob> getJobs(String user){
		return jobs.get(user);
	}
	public void addJob(String user, IndexingJob job) {
		ArrayList<IndexingJob> subset = this.jobs.get(user);
		if(subset == null) subset = new ArrayList<>();
		subset.add(job);
		this.jobs.put(user, subset);
	}
	
	public int jobCount(String user) {
		int activeJobs = 0;
		for(IndexingJob j : jobs.get(user)) {
			if(!j.isEnded())activeJobs++;
		}
		return activeJobs;
	}
}
