package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;
import java.util.Optional;

public record SkyboxData(
	Identifier id,
	Optional<ClientAsset.ResourceTexture> texture,
	float rotation,
	float rotating,
	Color tint,
	boolean celestials,
	boolean sun,
	boolean moon,
	Optional<Float> stars,
	Optional<Rotation> celestialRotation
) {
	public static final Codec<SkyboxData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("id").forGetter(SkyboxData::id),
		ClientAsset.ResourceTexture.CODEC.optionalFieldOf("texture").forGetter(SkyboxData::texture),
		Codec.FLOAT.optionalFieldOf("rotation", 0F).forGetter(SkyboxData::rotation),
		Codec.FLOAT.optionalFieldOf("rotating", 0F).forGetter(SkyboxData::rotating),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(SkyboxData::tint),
		Codec.BOOL.optionalFieldOf("celestials", false).forGetter(SkyboxData::celestials),
		Codec.BOOL.optionalFieldOf("sun", true).forGetter(SkyboxData::sun),
		Codec.BOOL.optionalFieldOf("moon", true).forGetter(SkyboxData::moon),
		Codec.FLOAT.optionalFieldOf("stars").forGetter(SkyboxData::stars),
		Rotation.CODEC.optionalFieldOf("celestial_rotation").forGetter(SkyboxData::celestialRotation)
	).apply(instance, SkyboxData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SkyboxData> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, SkyboxData::id,
		ByteBufCodecs.optional(ClientAsset.ResourceTexture.STREAM_CODEC), SkyboxData::texture,
		ByteBufCodecs.FLOAT, SkyboxData::rotation,
		ByteBufCodecs.FLOAT, SkyboxData::rotating,
		Color.STREAM_CODEC, SkyboxData::tint,
		ByteBufCodecs.BOOL, SkyboxData::celestials,
		ByteBufCodecs.BOOL, SkyboxData::sun,
		ByteBufCodecs.BOOL, SkyboxData::moon,
		ByteBufCodecs.optional(ByteBufCodecs.FLOAT), SkyboxData::stars,
		ByteBufCodecs.optional(Rotation.STREAM_CODEC), SkyboxData::celestialRotation,
		SkyboxData::new
	);

	public static final DataType<SkyboxData> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);

	public static final VLRegistry<SkyboxData> REGISTRY = VLRegistry.createClient("skybox", SkyboxData.class);

	public static final DataType<Identifier> ID_DATA_TYPE = DataType.of(ID.CODEC, ID.STREAM_CODEC);

	public static class Loader extends JsonRegistryReloadListener<SkyboxData> {
		public Loader() {
			super("vidlib/skybox", CODEC, true, REGISTRY);
		}

		@Override
		protected void apply(ResourceManager resourceManager, Map<Identifier, SkyboxData> map) {
			super.apply(resourceManager, map);
			SKYBOX_IDS.clear();
			SKYBOX_IDS.addAll(map.keySet());
			SKYBOX_IDS.sort(Identifier::compareNamespaced);
		}
	}
}
