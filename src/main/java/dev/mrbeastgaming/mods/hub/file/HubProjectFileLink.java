package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import dev.mrbeastgaming.mods.hub.api.HubFileType;

import java.util.List;

public record HubProjectFileLink(
	Checksum checksum,
	Checksum uniqueId,
	String path,
	HubFileType fileType,
	PossibleUser assignedTo
) {
	public static final Codec<HubProjectFileLink> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.fieldOf("checksum").forGetter(HubProjectFileLink::checksum),
		Checksum.CODEC.optionalFieldOf("unique_id", NoChecksum.INSTANCE).forGetter(HubProjectFileLink::uniqueId),
		Codec.STRING.fieldOf("path").forGetter(HubProjectFileLink::path),
		HubFileType.CODEC.optionalFieldOf("file_type", HubFileType.UNKNOWN).forGetter(HubProjectFileLink::fileType),
		PossibleUser.CODEC.optionalFieldOf("assigned_to", PossibleUser.NONE).forGetter(HubProjectFileLink::assignedTo)
	).apply(i, HubProjectFileLink::new));

	public static final Codec<List<HubProjectFileLink>> LIST_CODEC = CODEC.listOf();
}