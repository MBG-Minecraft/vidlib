package dev.mrbeastgaming.mods.hub.api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.ExtraCodecs;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

public record HubReplay(
	HubProjectUpload upload,
	Optional<URI> iconUrl,
	Optional<URI> previewUrl,
	float previewAspectRatio,
	Optional<URI> userIconUrl,
	String userIconName,
	JsonElement partialMetadata,
	Map<Codec<?>, Optional<Object>> parsedPartialMetadata
) {
	public static final Codec<HubReplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubProjectUpload.CODEC.fieldOf("upload").forGetter(HubReplay::upload),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("icon_url").forGetter(HubReplay::iconUrl),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("preview_url").forGetter(HubReplay::previewUrl),
		Codec.FLOAT.optionalFieldOf("preview_aspect_ratio", 16F / 9F).forGetter(HubReplay::previewAspectRatio),
		HubAPI.URI_BASE_CODEC.optionalFieldOf("user_icon_url").forGetter(HubReplay::userIconUrl),
		Codec.STRING.optionalFieldOf("user_icon_name", "").forGetter(HubReplay::userIconName),
		ExtraCodecs.JSON.optionalFieldOf("partial_metadata", new JsonObject()).forGetter(HubReplay::partialMetadata)
	).apply(instance, HubReplay::new));

	private HubReplay(
		HubProjectUpload upload,
		Optional<URI> iconUrl,
		Optional<URI> previewUrl,
		float previewAspectRatio,
		Optional<URI> userIconUrl,
		String userIconName,
		JsonElement partialMetadata
	) {
		this(
			upload,
			iconUrl,
			previewUrl,
			previewAspectRatio,
			userIconUrl,
			userIconName,
			partialMetadata,
			new Reference2ObjectOpenHashMap<>(1)
		);
	}

	public <T> Optional<T> getPartialMetadata(DynamicOps<JsonElement> jsonOps, Codec<T> codec) {
		//noinspection unchecked,rawtypes
		return (Optional<T>) parsedPartialMetadata.computeIfAbsent(codec, c -> (Optional) c.parse(jsonOps, partialMetadata).resultOrPartial());
	}
}
