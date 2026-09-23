package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.Hex32;
import net.minecraft.Util;

import java.util.UUID;

public record HubMinecraftProfile(
	Hex32 id,
	UUID uuid,
	String name,
	int modelType,
	String skinUrl,
	String capeUrl
) implements HubDBObject {
	public static final MapCodec<HubMinecraftProfile> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		HubDBObject.ID_FIELD.forGetter(HubMinecraftProfile::id),
		KLibCodecs.DASHED_UUID.optionalFieldOf("uuid", Util.NIL_UUID).forGetter(HubMinecraftProfile::uuid),
		Codec.STRING.optionalFieldOf("name", "Unknown").forGetter(HubMinecraftProfile::name),
		Codec.INT.optionalFieldOf("model_type", 0).forGetter(HubMinecraftProfile::modelType),
		Codec.STRING.optionalFieldOf("skin_url", "").forGetter(HubMinecraftProfile::skinUrl),
		Codec.STRING.optionalFieldOf("cape_url", "").forGetter(HubMinecraftProfile::capeUrl)
	).apply(i, HubMinecraftProfile::new));

	public static final Codec<HubMinecraftProfile> CODEC = MAP_CODEC.codec();


	public record LinkData(HubMinecraftProfile profile, String token) {
		public static final Codec<LinkData> CODEC = RecordCodecBuilder.create(i -> i.group(
			HubMinecraftProfile.MAP_CODEC.forGetter(LinkData::profile),
			Codec.STRING.optionalFieldOf("token", "").forGetter(LinkData::token)
		).apply(i, LinkData::new));
	}
}
