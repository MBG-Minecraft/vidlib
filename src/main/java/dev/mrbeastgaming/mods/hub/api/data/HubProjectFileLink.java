package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;

import java.util.List;

public record HubProjectFileLink(
	Checksum checksum,
	Checksum uniqueId,
	String path,
	HubFileType fileType,
	HubPossibleUser assignedTo
) {
	public static final Codec<HubProjectFileLink> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.fieldOf("checksum").forGetter(HubProjectFileLink::checksum),
		Checksum.CODEC.optionalFieldOf("unique_id", NoChecksum.INSTANCE).forGetter(HubProjectFileLink::uniqueId),
		Codec.STRING.fieldOf("path").forGetter(HubProjectFileLink::path),
		HubFileType.CODEC.optionalFieldOf("file_type", HubFileType.UNKNOWN).forGetter(HubProjectFileLink::fileType),
		HubPossibleUser.CODEC.optionalFieldOf("assigned_to", HubPossibleUser.NONE).forGetter(HubProjectFileLink::assignedTo)
	).apply(i, HubProjectFileLink::new));

	public static final Codec<List<HubProjectFileLink>> LIST_CODEC = CODEC.listOf();
}