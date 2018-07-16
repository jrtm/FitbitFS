package no.thrapmeyer.fitbitfs.loader;

import no.thrapmeyer.fitbitfs.fs.SnapshotNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FsSnapshotMerger {

	private final SnapshotNode fsSnapshot;

	private FsSnapshotMerger(Path rootPath) throws IOException {
		this.fsSnapshot = buildFsSnapshot(rootPath);
	}

	private SnapshotNode buildFsSnapshot(Path path) throws IOException {
		SnapshotNode root = new SnapshotNode();
		root.setName("_root");
		root.setDirectory(true);
		root.setChildren(buildChildren(root, path));
		return root;
	}

	private List<SnapshotNode> buildChildren(SnapshotNode parent, Path path) {
		try {
			return Files.walk(path, 1)
					.filter(this::isSafeFile)
					.filter(c -> !isSameFiles(c, path))
					.map(c -> {
						SnapshotNode child = new SnapshotNode();
						child.setName(c.getFileName().toString());
						child.setLastModifiedLocal(getLastModifiedTime(c));
						child.setDirectory(Files.isDirectory(c));
						child.setParent(parent);

						if (child.isDirectory()) {
							child.setChildren(buildChildren(child, c));
						}
						return child;
					}).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Could not build children tree for path " + path, e);
		}

	}

	private long getLastModifiedTime(Path path) {
		try {
			return Files.getLastModifiedTime(path).toMillis();
		} catch (IOException e) {
			throw new RuntimeException("Unable to get modified time for file " + path, e);
		}
	}

	private boolean isSafeFile(Path path) {
		try {
			return !Files.isHidden(path) && Files.isReadable(path);
		} catch (IOException e) {
			throw new RuntimeException("Unable to determine file state for path " + path, e);
		}
	}

	private boolean isSameFiles(Path a, Path b) {
		try {
			return Files.isSameFile(a, b);
		} catch (IOException e) {
			throw new RuntimeException("Unable to determine whether files are same: " + a + ", " + b, e);
		}
	}

	public static SnapshotNode mergeLocalWithRemoteSnapshots(Path localPath, SnapshotNode remoteSnapshot) throws IOException {
		return SnapshotNode.getMerged(new FsSnapshotMerger(localPath).fsSnapshot, remoteSnapshot);
	}
}

