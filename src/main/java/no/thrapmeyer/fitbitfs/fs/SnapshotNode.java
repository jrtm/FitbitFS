package no.thrapmeyer.fitbitfs.fs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnapshotNode {

	private String id;

	private boolean directory;

	private String name;

	private LocalDateTime lastModifiedRemote;

	private long lastModifiedLocal;

	@JsonIgnore
	private SnapshotNode parent;

	private List<SnapshotNode> children = new ArrayList<>();

	private MergeType type;

	public MergeType getType() {
		return type;
	}

	public void setType(MergeType type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isFile() {
		return !isDirectory();
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getLastModifiedRemote() {
		return lastModifiedRemote;
	}

	public void setLastModifiedRemote(LocalDateTime lastModifiedRemote) {
		this.lastModifiedRemote = lastModifiedRemote;
	}

	public long getLastModifiedLocal() {
		return lastModifiedLocal;
	}

	public void setLastModifiedLocal(long lastModifiedLocal) {
		this.lastModifiedLocal = lastModifiedLocal;
	}

	public SnapshotNode getParent() {
		return parent;
	}

	public void setParent(SnapshotNode parent) {
		this.parent = parent;
	}

	public List<SnapshotNode> getChildren() {
		return children;
	}

	public void setChildren(List<SnapshotNode> children) {
		this.children = children;
	}

	public Path toPath() {
		if (parent == null) {
			return null;
		}

		Path parentPath = parent.toPath();
		if (parentPath == null) return Paths.get(name);

		return parentPath.resolve(name);
	}

	public String toUrl() {
		String pathComponent = name;
		if (parent == null) return "";
		return parent.toUrl() + "/" + pathComponent;
	}

	public boolean isLocallyModifiedAfter(SnapshotNode node) {
		return lastModifiedLocal> node.lastModifiedLocal;
	}


	public boolean isRemotelyModifiedAfter(SnapshotNode node) {
		return lastModifiedRemote.isAfter(node.lastModifiedRemote);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FsNode{");
		sb.append("id='").append(id).append('\'');
		sb.append(", directory=").append(directory);
		sb.append(", name='").append(name).append('\'');
		sb.append(", lastModifiedRemote=").append(lastModifiedRemote);
		sb.append(", lastModifiedLocal=").append(lastModifiedLocal);
		sb.append(", children=").append(children);
		sb.append('}');
		return sb.toString();
	}


	public static SnapshotNode getMerged(SnapshotNode local, SnapshotNode remote) {
		checkConflicts(local, remote);
		SnapshotNode node = new SnapshotNode();
		node.setType(local == null ? MergeType.REMOTE_ONLY : remote == null ? MergeType.LOCAL_ONLY : MergeType.BOTH);

		if (remote != null) {
			node.setId(remote.getId());
			node.setLastModifiedRemote(remote.getLastModifiedRemote());
			node.setDirectory(remote.isDirectory());
			node.setName(remote.getName());
		}

		if (local != null) {
			node.setName(local.getName());
			node.setLastModifiedLocal(local.getLastModifiedLocal());
			node.setDirectory(local.isDirectory());
		}

		if (node.isDirectory()) {
			node.setChildren(getMergedChildren(local == null ? null : local.getChildren(), remote == null ? null : remote.getChildren()));
			node.getChildren().forEach(child -> child.setParent(node));
		}

		return node;
	}

	private static List<SnapshotNode> getMergedChildren(List<SnapshotNode> localChildren, List<SnapshotNode> remoteChildren) {
		if (localChildren == null) {
			return remoteChildren.stream().map(node -> getMerged(null, node)).collect(Collectors.toList());
		}
		if (remoteChildren == null) {
			return localChildren.stream().map(node -> getMerged(node, null)).collect(Collectors.toList());
		}

		Map<String, SnapshotNode> localNames = localChildren.stream().collect(Collectors.toMap(node -> node.getName(), Function.identity()));
		Map<String, SnapshotNode> remoteNames = remoteChildren.stream().collect(Collectors.toMap(node -> node.getName(), Function.identity()));

		Set<String> allNames = new HashSet<>();
		allNames.addAll(localNames.keySet());
		allNames.addAll(remoteNames.keySet());

		return allNames.stream()
				.map((name) -> getMerged(localNames.get(name), remoteNames.get(name)))
				.collect(Collectors.toList());
	}

	private static void checkConflicts(SnapshotNode local, SnapshotNode remote) {
		if (local == null || remote == null) return;

		if (!local.getName().equals(remote.getName())) throw new MergeException("File names not equal in mrege", local, remote);
		if (local.isDirectory() != remote.isDirectory()) throw new MergeException("Nodes are not of same type (directory/file)", local, remote);
	}

	public boolean isRoot() {
		return parent == null;
	}
}
