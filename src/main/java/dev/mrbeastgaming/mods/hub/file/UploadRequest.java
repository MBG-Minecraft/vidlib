package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record UploadRequest(
	String gatewayToken,
	List<UploadRequestFile> files
) {
	public static final Codec<UploadRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("gateway_token").forGetter(UploadRequest::gatewayToken),
		UploadRequestFile.CODEC.listOf().fieldOf("files").forGetter(UploadRequest::files)
	).apply(i, UploadRequest::new));
}
