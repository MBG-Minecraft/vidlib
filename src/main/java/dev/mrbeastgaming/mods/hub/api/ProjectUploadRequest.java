package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.data.ProjectUploadRequestItem;

import java.util.List;

public record ProjectUploadRequest(
	String projectToken,
	List<ProjectUploadRequestItem> files
) {
	public static final Codec<ProjectUploadRequest> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("project_token").forGetter(ProjectUploadRequest::projectToken),
		ProjectUploadRequestItem.CODEC.listOf().fieldOf("files").forGetter(ProjectUploadRequest::files)
	).apply(i, ProjectUploadRequest::new));
}
