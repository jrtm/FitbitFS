package no.thrapmeyer.fitbitfs.fdb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FitbitHost {

	private static final String ROLE_APP_HOST = "APP_HOST";
	private static final String ROLE_COMPANION_HOST = "COMPANION_HOST";

	private static final String STATE_AVAILABLE = "available";

	private String id;

	private String displayName;

	private Set<String> roles = new HashSet<>();

	private String state;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isAppHost() {
		return roles.contains(ROLE_APP_HOST);
	}

	public boolean isCompanionHost() {
		return roles.contains(ROLE_COMPANION_HOST);
	}

	public boolean isAvailable() {
		return STATE_AVAILABLE.equals(state);
	}
}