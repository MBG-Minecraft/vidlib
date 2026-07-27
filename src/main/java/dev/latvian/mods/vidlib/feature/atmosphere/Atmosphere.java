package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.attribute.EnvironmentAttributeMap;

import java.util.Optional;

public record Atmosphere(
	Optional<SkyboxTextureData> skybox,
	EnvironmentAttributeMap attributes,
	Optional<Boolean> sun,
	Optional<Boolean> moon,
	Optional<Rotation> celestialRotation
) implements CustomRegistryValue<RegistryFriendlyByteBuf, Atmosphere> {
	public static final DynamicType<RegistryFriendlyByteBuf, Atmosphere> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			SkyboxTextureData.CODEC.optionalFieldOf("skybox_texture").forGetter(Atmosphere::skybox),
			EnvironmentAttributeMap.CODEC.optionalFieldOf("attributes", EnvironmentAttributeMap.EMPTY).forGetter(Atmosphere::attributes),
			Codec.BOOL.optionalFieldOf("sun").forGetter(Atmosphere::sun),
			Codec.BOOL.optionalFieldOf("moon").forGetter(Atmosphere::moon),
			Rotation.CODEC_WITH_ROLL.optionalFieldOf("celestial_rotation").forGetter(Atmosphere::celestialRotation)
		).apply(instance, Atmosphere::new)),
		CompositeStreamCodec.of(
			ByteBufCodecs.optional(SkyboxTextureData.STREAM_CODEC), Atmosphere::skybox,
			KLibStreamCodecs.optional(ByteBufCodecs.fromCodecTrusted(EnvironmentAttributeMap.NETWORK_CODEC), EnvironmentAttributeMap.EMPTY), Atmosphere::attributes,
			ByteBufCodecs.optional(ByteBufCodecs.BOOL), Atmosphere::sun,
			ByteBufCodecs.optional(ByteBufCodecs.BOOL), Atmosphere::moon,
			ByteBufCodecs.optional(Rotation.STREAM_CODEC_WITH_ROLL), Atmosphere::celestialRotation,
			Atmosphere::new
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, Atmosphere> REGISTRY = CustomRegistry.createNoValueSync("atmosphere", TYPE);

	public static final Codec<Ref<Atmosphere>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<Atmosphere>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<Atmosphere>> DATA_TYPE = REGISTRY.dataType();

	public static final ImBuilderType<Ref<Atmosphere>> IM_BUILDER_TYPE = EnumImBuilder.of(REGISTRY).nullName("Vanilla").buildType();

	public static class ClientLoader extends JsonRegistryReloadListener<Atmosphere> {
		public ClientLoader() {
			super("vidlib/atmosphere", REGISTRY);
		}
	}

	@Override
	public CustomRegistry<RegistryFriendlyByteBuf, Atmosphere> getRegistry() {
		return REGISTRY;
	}

	@Override
	public DynamicType<RegistryFriendlyByteBuf, Atmosphere> type() {
		return TYPE;
	}
}
