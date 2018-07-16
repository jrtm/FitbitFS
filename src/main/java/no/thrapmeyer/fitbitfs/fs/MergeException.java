package no.thrapmeyer.fitbitfs.fs;

import no.thrapmeyer.fitbitfs.fs.SnapshotNode;

public class MergeException extends RuntimeException {

	private final SnapshotNode localNode;
	private final SnapshotNode remoteNode;

	public MergeException(String message, SnapshotNode local, SnapshotNode remote) {
		super(message);
		this.localNode = local;
		this.remoteNode = remote;
	}

	public SnapshotNode getLocalNode() {
		return localNode;
	}

	public SnapshotNode getRemoteNode() {
		return remoteNode;
	}
}
