package dev.mrbeastgaming.mods.hub.file;

import java.io.IOException;

@FunctionalInterface
public interface HubUploadBuilderCallback {
	void build(HubUploadBuilder builder) throws IOException;

	default HubUploadBuilderCallback then(HubUploadBuilderCallback callback) {
		return (builder) -> {
			build(builder);
			callback.build(builder);
		};
	}
}
