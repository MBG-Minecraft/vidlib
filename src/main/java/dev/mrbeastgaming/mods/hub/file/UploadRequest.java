package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubUploadRequestFile;

import java.util.List;

public record UploadRequest(
	String gatewayToken,
	List<HubUploadRequestFile> files
) {
	public static final Codec<UploadRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("gateway_token").forGetter(UploadRequest::gatewayToken),
		HubUploadRequestFile.CODEC.listOf().fieldOf("files").forGetter(UploadRequest::files)
	).apply(i, UploadRequest::new));
}
