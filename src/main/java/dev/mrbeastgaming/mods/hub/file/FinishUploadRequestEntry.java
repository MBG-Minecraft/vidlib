package dev.mrbeastgaming.mods.hub.file;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.io.checksum.Checksum;

import java.util.List;

public record FinishUploadRequestEntry(
	Checksum checksum,
	List<HubFileAction> actions
) {
	public static final Codec<FinishUploadRequestEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
		Checksum.CODEC.fieldOf("checksum").forGetter(FinishUploadRequestEntry::checksum),
		HubFileAction.CODEC.listOf().fieldOf("actions").forGetter(FinishUploadRequestEntry::actions)
	).apply(i, FinishUploadRequestEntry::new));
}
