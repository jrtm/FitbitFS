package no.thrapmeyer.fitbitfs.sync;

public class SyncConfig {

	private String projectId;

	private String jwt;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}
}
