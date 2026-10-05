package dev.mrbeastgaming.mods.hub.file;

import dev.mrbeastgaming.mods.hub.api.data.HubChecksumPath;

public record HubUploadResponseItem(
	Status status,
	HubUploadRequestItem request
) {
	public enum Status {
		SKIPPED,
		HTTP,
		GATEWAY
	}

	public boolean notSkipped() {
		return status != Status.SKIPPED;
	}

	public HubChecksumPath toChecksumPath() {
		return new HubChecksumPath(request.info().checksum(), request.info().path());
	}
}
