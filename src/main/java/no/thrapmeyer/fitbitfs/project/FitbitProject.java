package no.thrapmeyer.fitbitfs.project;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class FitbitProject {

	private int status;

	private String name;

	@JsonProperty("fs_nodes")
	private List<ProjectNode> fsNodes;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProjectNode> getFsNodes() {
		return fsNodes;
	}

	public void setFsNodes(List<ProjectNode> fsNodes) {
		this.fsNodes = fsNodes;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FitbitProject{");
		sb.append("status=").append(status);
		sb.append(", name='").append(name).append('\'');
		sb.append(", fsNodes=").append(fsNodes);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FitbitProject that = (FitbitProject) o;
		return status == that.status &&
				Objects.equals(name, that.name) &&
				Objects.equals(fsNodes, that.fsNodes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, name, fsNodes);
	}

}
