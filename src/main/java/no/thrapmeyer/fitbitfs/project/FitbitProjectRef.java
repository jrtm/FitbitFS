package no.thrapmeyer.fitbitfs.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class FitbitProjectRef {

	private String id;

	private String name;

	@JsonProperty("last_modified")
	private ZonedDateTime lastModified;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ZonedDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(ZonedDateTime lastModified) {
		this.lastModified = lastModified;
	}
}
