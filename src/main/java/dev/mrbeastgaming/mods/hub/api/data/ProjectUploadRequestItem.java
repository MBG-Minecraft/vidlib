package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;

import java.time.Instant;

public record ProjectUploadRequestItem(
	Checksum uniqueId,
	Checksum checksum,
	long size,
	String name,
	HubFileType type,
	Instant created,
	HubPossibleUser assignedTo
) {
	public static final Codec<ProjectUploadRequestItem> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.optionalFieldOf("unique_id", NoChecksum.INSTANCE).forGetter(ProjectUploadRequestItem::uniqueId),
		Checksum.CODEC.optionalFieldOf("checksum", NoChecksum.INSTANCE).forGetter(ProjectUploadRequestItem::checksum),
		Codec.LONG.optionalFieldOf("size", 0L).forGetter(ProjectUploadRequestItem::size),
		Codec.STRING.optionalFieldOf("name", "").forGetter(ProjectUploadRequestItem::name),
		HubFileType.CODEC.optionalFieldOf("type", HubFileType.UNKNOWN).forGetter(ProjectUploadRequestItem::type),
		KLibCodecs.INSTANT.optionalFieldOf("created", Instant.EPOCH).forGetter(ProjectUploadRequestItem::created),
		HubPossibleUser.CODEC.optionalFieldOf("assigned_to", HubPossibleUser.NONE).forGetter(ProjectUploadRequestItem::assignedTo)
	).apply(i, ProjectUploadRequestItem::new));

	public String ref() {
		return uniqueId.isNil() ? ("c-" + checksum) : ("u-" + uniqueId);
	}
}
