package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;

import java.util.UUID;

public record HubMinecraftProfileData(
	Hex32 id,
	UUID uuid,
	String name,
	int modelType,
	String skinUrl,
	String capeUrl
) {
	public static final Codec<HubMinecraftProfileData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubMinecraftProfileData::id),
		KLibCodecs.DASHED_UUID.fieldOf("uuid").forGetter(HubMinecraftProfileData::uuid),
		Codec.STRING.fieldOf("name").forGetter(HubMinecraftProfileData::name),
		Codec.INT.optionalFieldOf("model_type", 0).forGetter(HubMinecraftProfileData::modelType),
		Codec.STRING.optionalFieldOf("skin_url", "").forGetter(HubMinecraftProfileData::skinUrl),
		Codec.STRING.optionalFieldOf("cape_url", "").forGetter(HubMinecraftProfileData::capeUrl)
	).apply(instance, HubMinecraftProfileData::new));

	public static HubMinecraftProfileData SELF = null;
}
