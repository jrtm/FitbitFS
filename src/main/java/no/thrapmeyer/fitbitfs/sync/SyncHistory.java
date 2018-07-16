package no.thrapmeyer.fitbitfs.sync;

import no.thrapmeyer.fitbitfs.fs.SnapshotNode;

import java.time.LocalDateTime;

public class SyncHistory {

	private LocalDateTime syncDate;

	private SnapshotNode snapshotNode;

	public LocalDateTime getSyncDate() {
		return syncDate;
	}

	public void setSyncDate(LocalDateTime syncDate) {
		this.syncDate = syncDate;
	}

	public SnapshotNode getSnapshotNode() {
		return snapshotNode;
	}

	public void setSnapshotNode(SnapshotNode snapshotNode) {
		this.snapshotNode = snapshotNode;
	}
}
