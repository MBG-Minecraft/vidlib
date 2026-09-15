package dev.mrbeastgaming.mods.hub.api.project;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import net.minecraft.util.ExtraCodecs;

import java.net.URI;
import java.time.Instant;

public record HubReplayData(
	String upload,
	String name,
	long size,
	URI uri,
	Checksum checksum,
	Instant created,
	Instant uploaded,
	Hex32 uploadedBy,
	Hex32 assignedTo,
	JsonElement partialMetadata
) {
	public static final Codec<HubReplayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("upload").forGetter(HubReplayData::upload),
		Codec.STRING.fieldOf("name").forGetter(HubReplayData::name),
		Codec.LONG.fieldOf("size").forGetter(HubReplayData::size),
		HubAPI.URI_BASE_CODEC.fieldOf("uri").forGetter(HubReplayData::uri),
		Checksum.CODEC.fieldOf("checksum").forGetter(HubReplayData::checksum),
		KLibCodecs.INSTANT.fieldOf("created").forGetter(HubReplayData::created),
		KLibCodecs.INSTANT.fieldOf("uploaded").forGetter(HubReplayData::uploaded),
		Hex32.CODEC.fieldOf("uploaded_by").forGetter(HubReplayData::uploadedBy),
		Hex32.CODEC.fieldOf("assigned_to").forGetter(HubReplayData::assignedTo),
		ExtraCodecs.JSON.fieldOf("partial_metadata").forGetter(HubReplayData::partialMetadata)
	).apply(instance, HubReplayData::new));
}
