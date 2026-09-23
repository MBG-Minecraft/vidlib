package dev.mrbeastgaming.mods.hub.api.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.util.Hex32;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.ExtraCodecs;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public record HubReplay(
	String name,
	long size,
	URI url,
	Optional<URI> iconUrl,
	Checksum checksum,
	Instant created,
	Instant uploaded,
	Hex32 uploadedBy,
	Hex32 assignedTo,
	JsonElement partialMetadata,
	Map<Codec<?>, Optional<Object>> parsedPartialMetadata
) {
	public static final Codec<HubReplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(HubReplay::name),
		Codec.LONG.fieldOf("size").forGetter(HubReplay::size),
		HubAPI.URI_BASE_CODEC.fieldOf("url").forGetter(HubReplay::url),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("icon_url").forGetter(HubReplay::iconUrl),
		Checksum.CODEC.fieldOf("checksum").forGetter(HubReplay::checksum),
		KLibCodecs.INSTANT.optionalFieldOf("created", Instant.EPOCH).forGetter(HubReplay::created),
		KLibCodecs.INSTANT.optionalFieldOf("uploaded", Instant.EPOCH).forGetter(HubReplay::uploaded),
		HubDBObject.idCodec("uploaded_by").forGetter(HubReplay::uploadedBy),
		HubDBObject.idCodec("assigned_to").forGetter(HubReplay::assignedTo),
		ExtraCodecs.JSON.fieldOf("partial_metadata").forGetter(HubReplay::partialMetadata)
	).apply(instance, HubReplay::new));

	private HubReplay(
		String name,
		long size,
		URI url,
		Optional<URI> iconUrl,
		Checksum checksum,
		Instant created,
		Instant uploaded,
		Hex32 uploadedBy,
		Hex32 assignedTo,
		JsonElement partialMetadata
	) {
		this(
			name,
			size,
			url,
			iconUrl,
			checksum,
			created,
			uploaded,
			uploadedBy,
			assignedTo,
			partialMetadata,
			new Reference2ObjectOpenHashMap<>(1)
		);
	}

	public <T> Optional<T> getPartialMetadata(Codec<T> codec) {
		//noinspection unchecked,rawtypes
		return (Optional<T>) parsedPartialMetadata.computeIfAbsent(codec, c -> (Optional) c.parse(JsonOps.INSTANCE, partialMetadata).resultOrPartial());
	}
}
