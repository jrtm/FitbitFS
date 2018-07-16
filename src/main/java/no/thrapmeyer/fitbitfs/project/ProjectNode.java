package no.thrapmeyer.fitbitfs.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;

public class ProjectNode {

	private String id;

	private String name;

	@JsonProperty("last_modified")
	private ZonedDateTime lastModified;

	private String parent;

	private String type;

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

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FsNode{");
		sb.append("id='").append(id).append('\'');
		sb.append(", name='").append(name).append('\'');
		sb.append(", lastModified='").append(lastModified).append('\'');
		sb.append(", parent='").append(parent).append('\'');
		sb.append(", type='").append(type).append('\'');
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectNode fsNode = (ProjectNode) o;
		return Objects.equals(id, fsNode.id) &&
				Objects.equals(name, fsNode.name) &&
				Objects.equals(lastModified, fsNode.lastModified) &&
				Objects.equals(parent, fsNode.parent) &&
				Objects.equals(type, fsNode.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, lastModified, parent, type);
	}

	public boolean isRoot() {
		return parent == null;
	}
}
