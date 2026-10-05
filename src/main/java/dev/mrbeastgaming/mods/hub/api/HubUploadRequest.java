package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.HubUploadRequestFile;

import java.util.List;

public record HubUploadRequest(
	String intent,
	String gatewayToken,
	List<HubUploadRequestFile> files
) {
	public static final Codec<HubUploadRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.optionalFieldOf("intent", "").forGetter(HubUploadRequest::intent),
		Codec.STRING.optionalFieldOf("gateway_token", "").forGetter(HubUploadRequest::gatewayToken),
		HubUploadRequestFile.CODEC.listOf().fieldOf("files").forGetter(HubUploadRequest::files)
	).apply(i, HubUploadRequest::new));
}
