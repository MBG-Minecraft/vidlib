package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;

public record HubProjectFileLinkInfo(
	Checksum uniqueId,
	String path,
	HubFileType fileType,
	HubPossibleUser assignedTo,
	boolean manual
) {
	public static final MapCodec<HubProjectFileLinkInfo> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Checksum.CODEC.optionalFieldOf("unique_id", NoChecksum.INSTANCE).forGetter(HubProjectFileLinkInfo::uniqueId),
		Codec.STRING.fieldOf("path").forGetter(HubProjectFileLinkInfo::path),
		HubFileType.CODEC.optionalFieldOf("file_type", HubFileType.UNKNOWN).forGetter(HubProjectFileLinkInfo::fileType),
		HubPossibleUser.CODEC.optionalFieldOf("assigned_to", HubPossibleUser.NONE).forGetter(HubProjectFileLinkInfo::assignedTo),
		Codec.BOOL.fieldOf("manual").forGetter(HubProjectFileLinkInfo::manual)
	).apply(i, HubProjectFileLinkInfo::new));
}