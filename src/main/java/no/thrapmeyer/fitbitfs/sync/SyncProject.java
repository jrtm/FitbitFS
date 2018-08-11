package no.thrapmeyer.fitbitfs.sync;

import no.thrapmeyer.fitbitfs.fs.FitbitFsSnapshot;
import no.thrapmeyer.fitbitfs.fs.MergeType;
import no.thrapmeyer.fitbitfs.fs.SnapshotNode;
import no.thrapmeyer.fitbitfs.http.FitbitHttpClient;
import no.thrapmeyer.fitbitfs.project.FitbitProject;
import no.thrapmeyer.fitbitfs.loader.FsSnapshotMerger;
import no.thrapmeyer.fitbitfs.loader.ProjectToFsSnapshot;
import no.thrapmeyer.fitbitfs.util.AnsiColor;
import no.thrapmeyer.fitbitfs.util.Util;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SyncProject {

	private final String jwt;

	private final Path root;

	public SyncProject(String jwt, Path root) {
		this.jwt = jwt;
		this.root = root;
	}

	public Path getMetadataFolderPath() {
		return root.resolve(".fitbitfs");
	}

	public Path getConfigPath() {
		return getMetadataFolderPath().resolve("config.json");
	}

	public SyncConfig readConfig() throws IOException {
		Path configPath = getConfigPath();
		if (!Files.exists(configPath)) {
			return null;
		}
		return Util.objectMapper.readValue(configPath.toFile(), SyncConfig.class);
	}


	public Path getHistoryPath() {
		return getMetadataFolderPath().resolve("lastSync.json");
	}

	public SyncHistory readHistory() throws IOException {
		Path historyPath = getHistoryPath();
		if (!Files.exists(historyPath)) {
			return null;
		}
		SyncHistory history = Util.objectMapper.readValue(historyPath.toFile(), SyncHistory.class);
		rebuildParents(history.getSnapshotNode());
		return history;
	}

	private void rebuildParents(SnapshotNode node) {
		if (node.isFile()) return;
		node.getChildren().forEach(child -> {
			child.setParent(node);
			rebuildParents(child);
		});
	}

	public void writeHistory(SyncHistory history) throws IOException {
		Path historyPath = getHistoryPath();
		Util.objectMapper.writeValue(historyPath.toFile(), history);
	}

	public void initFs(SyncConfig config) throws IOException {
		if (!Files.exists(getMetadataFolderPath())) {
			Files.createDirectory(getMetadataFolderPath());
		}
		byte[] data = Util.objectMapper.writeValueAsBytes(config);
		Files.write(getConfigPath(), data);
	}

	public void synchronize() throws IOException {
		SyncConfig config = readConfig();

		long startTime = System.currentTimeMillis();

		FitbitHttpClient client = new FitbitHttpClient(jwt);
		FitbitProject project = client.getProject(config.getProjectId());
		System.out.println("Found project " + AnsiColor.cyan(project.getName()));

		FitbitFsSnapshot snapshot = new ProjectToFsSnapshot(project).toFs();
		SnapshotNode mergedSnapshotNode = FsSnapshotMerger.mergeLocalWithRemoteSnapshots(root, snapshot.getRootNode());

		SyncHistory history = readHistory();
		if (history == null) {


			List<SnapshotNode> newNodes = getAllForType(mergedSnapshotNode, MergeType.REMOTE_ONLY);

			newNodes.stream()
					.filter(SnapshotNode::isDirectory)
					.forEach(node -> mkdirs(root.resolve(node.toPath())));

			newNodes.parallelStream()
					.filter(SnapshotNode::isFile)
					.forEach(node -> {
						downloadFile(config, client, node);
			});


		} else {
			SnapshotNode lastSyncNode = history.getSnapshotNode();
			Map<Path, SnapshotNode> oldPathMap = toPathMap(lastSyncNode);
			Map<Path, SnapshotNode> newPathMap = toPathMap(mergedSnapshotNode);

			Set<Path> onlyNew = new HashSet<>(newPathMap.keySet());
			onlyNew.removeAll(oldPathMap.keySet());

			Set<Path> both = new HashSet<>(oldPathMap.keySet());
			both.retainAll(newPathMap.keySet());

			System.out.println("Found " + (both.size() + onlyNew.size()) + " files (" +onlyNew.size() + " new, " + both.size() + " existing)");

			for (Path pathInBoth : both) {
				if (pathInBoth == null) continue;
				SnapshotNode oldNode = oldPathMap.get(pathInBoth);
				SnapshotNode newNode = newPathMap.get(pathInBoth);

				switch (newNode.getType()) {
					case REMOTE_ONLY:
						deleteFile(config, client, newNode);
						break;
					case LOCAL_ONLY:
						System.out.println(AnsiColor.red("Deleting local file " + pathInBoth));
						Files.delete(root.resolve(pathInBoth));
						break;
					case BOTH:
						if (newNode.isRemotelyModifiedAfter(oldNode)) {
							downloadFile(config, client, newNode);
						} else if (newNode.isLocallyModifiedAfter(oldNode)) {
							uploadFile(config, client, newNode);
						}
						break;
				}
			}

			for (Path pathInNew: onlyNew) {
				if (pathInNew == null) continue;
				SnapshotNode newNode = newPathMap.get(pathInNew);

				switch (newNode.getType()) {
					case REMOTE_ONLY:
						downloadFile(config, client, newNode);
						break;
					case LOCAL_ONLY:
						uploadNewDirs(config, client, newNode);
						uploadFile(config, client, newNode);
						break;
					case BOTH:
						throw new IllegalStateException("New file found both remotely and locally: " + pathInNew);
				}
			}
		}

		project = client.getProject(config.getProjectId());
		snapshot = new ProjectToFsSnapshot(project).toFs();
		SyncHistory updatedHistory = new SyncHistory();
		updatedHistory.setSnapshotNode(FsSnapshotMerger.mergeLocalWithRemoteSnapshots(root, snapshot.getRootNode()));
		updatedHistory.setSyncDate(LocalDateTime.now());
		writeHistory(updatedHistory);

		long endTime = System.currentTimeMillis();
		System.out.println("Synchronized in " + (endTime - startTime) + "ms");
	}

	private void uploadNewDirs(SyncConfig config, FitbitHttpClient client, SnapshotNode newNode) {
		if (newNode.getParent().isRoot() || newNode.getParent().getType() != MergeType.LOCAL_ONLY) return;

		List<SnapshotNode> parentsToCreate = new ArrayList<>();
		SnapshotNode parent = newNode.getParent();
		while (parent != null && !parent.isRoot() && parent.getType() == MergeType.LOCAL_ONLY) {
			parentsToCreate.add(parent);
			parent = parent.getParent();
		}

		if (parentsToCreate.isEmpty()) return;

		for (int i = parentsToCreate.size() - 1; i >= 0; --i) {
			// iterate backwards to create directories in the right order
			SnapshotNode node = parentsToCreate.get(i);
			System.out.println(AnsiColor.green("Creating new directory " + node.toPath() + " ..."));
			client.mkdir(config.getProjectId(), node.toUrl());
			System.out.println(AnsiColor.green("Directory " + node.toPath() + " created"));
		}
	}

	private void deleteFile(SyncConfig config, FitbitHttpClient client, SnapshotNode newNode) {
		System.out.println(AnsiColor.red("Deleting file " + newNode.toPath() + " ..."));
		client.deleteFile(config.getProjectId(), newNode.toUrl());
		System.out.println(AnsiColor.red("Deleted " + newNode.toPath()));
	}

	private void downloadFile(SyncConfig config, FitbitHttpClient client, SnapshotNode node) {
		if (node.isRoot() || node.isDirectory()) return;
		byte[] data = client.getFile(config.getProjectId(), node.toUrl());
		Path nodePath = node.toPath();
		System.out.println(AnsiColor.blue("Downloading " + nodePath + " ..."));
		writeFile(root.resolve(nodePath), data);
		System.out.println(AnsiColor.blue("Downloaded " + nodePath));
	}

	private void uploadFile(SyncConfig config, FitbitHttpClient client, SnapshotNode node) throws IOException {
		if (node.isRoot() || node.isDirectory()) return;
		byte[] data = Files.readAllBytes(root.resolve(node.toPath()));
		Path nodePath = node.toPath();
		System.out.println(AnsiColor.blue("Uploading " + nodePath + " ..."));
		client.updateFile(config.getProjectId(), node.toUrl(), data);
		System.out.println(AnsiColor.blue("Uploaded " + nodePath));
	}

	private Map<Path,SnapshotNode> toPathMap(SnapshotNode node) {
		Map<Path, SnapshotNode> map = new HashMap<>();
		addPathsToMap(node, map);
		return map;
	}

	private void addPathsToMap(SnapshotNode node, Map<Path, SnapshotNode> map) {
		if (node.isFile()) map.put(node.toPath(), node);
		else node.getChildren().forEach(child -> addPathsToMap(child, map));
	}

	private void writeFile(Path path, byte[] data) {
		try {
			byte[] bytesSafe = data == null? new byte[0] : data;
			Files.write(path, bytesSafe);
		} catch (IOException e) {
			throw new RuntimeException("Could not save file " + path, e);
		}
	}

	private void mkdirs(Path path) {
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			throw new RuntimeException("Could not create directories for path " + path, e);
		}
	}


	private List<SnapshotNode> getAllForType(SnapshotNode root, MergeType type) {
		List<SnapshotNode> nodes = new ArrayList<>();
		if (root != null) {
			if (root.getType() == type) nodes.add(root);
			if (root.isDirectory()) {
				root.getChildren().forEach(child -> nodes.addAll(getAllForType(child, type)));
			}
		}
		return nodes;
	}

	public Path getRootPath() {
		return root;
	}
}
