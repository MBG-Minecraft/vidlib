package dev.beast.mods.shimmer.feature.worldsync;

import java.util.UUID;

public record RemoteFile(String path, UUID checksum, long size, int index) {
	@Override
	public String toString() {
		return checksum + " " + size + " " + path;
	}

	@Override
	public int hashCode() {
		return checksum.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RemoteFile) {
			return ((RemoteFile) obj).checksum.equals(checksum);
		}

		return false;
	}
}