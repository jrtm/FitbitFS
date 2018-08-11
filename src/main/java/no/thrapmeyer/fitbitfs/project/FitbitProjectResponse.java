package no.thrapmeyer.fitbitfs.project;

import java.util.ArrayList;
import java.util.List;

public class FitbitProjectResponse {

	private List<FitbitProjectRef> projects = new ArrayList<>();

	public List<FitbitProjectRef> getProjects() {
		return projects;
	}

	public void setProjects(List<FitbitProjectRef> projects) {
		this.projects = projects;
	}
}
