package no.thrapmeyer.fitbitfs.loader;

import no.thrapmeyer.fitbitfs.fs.FitbitFsSnapshot;
import no.thrapmeyer.fitbitfs.fs.SnapshotNode;
import no.thrapmeyer.fitbitfs.project.FitbitProject;
import no.thrapmeyer.fitbitfs.project.ProjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectToFsSnapshot {

	private FitbitProject project;

	private final Map<String, List<ProjectNode>> nodesByParent;

	public ProjectToFsSnapshot(FitbitProject project) {
		this.project = project;

		List<ProjectNode> projectNodes = project.getFsNodes();

		this.nodesByParent = projectNodes.stream()
				.filter(node -> !node.isRoot())
				.collect(Collectors.groupingBy(ProjectNode::getParent));
	}

	public FitbitFsSnapshot toFs() {
		FitbitFsSnapshot fs = new FitbitFsSnapshot();

		ProjectNode rootNode = project.getFsNodes().stream()
				.filter(node -> node.isRoot())
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No root node found"));

		fs.setRootNode(toSnapshotNode(rootNode));
		return fs;
	}

	private SnapshotNode toSnapshotNode(ProjectNode projectNode) {
		SnapshotNode node = new SnapshotNode();
		node.setId(projectNode.getId());
		node.setName(projectNode.getName());
		node.setLastModifiedRemote(projectNode.getLastModified().toLocalDateTime());
		node.setDirectory(isDirectory(projectNode));

		if (node.isFile()) {
			return node;
		}

		List<ProjectNode> projectChildren = nodesByParent.getOrDefault(projectNode.getId(), new ArrayList<>());
		node.setChildren(projectChildren.stream()
				.map(this::toSnapshotNode)
				.peek(child-> child.setParent(node))
				.collect(Collectors.toList()));
		return node;
	}

	private boolean isDirectory(ProjectNode node) {
		return "application/vnd.cloudbit.directory".equals(node.getType());
	}
}
