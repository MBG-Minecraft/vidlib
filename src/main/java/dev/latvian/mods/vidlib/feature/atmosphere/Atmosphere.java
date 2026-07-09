package dev.latvian.mods.vidlib.feature.atmosphere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.registry.RegistryKeys;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributeMap;

import java.util.Optional;

public record Atmosphere(
	Identifier id,
	Optional<SkyboxTextureData> skybox,
	EnvironmentAttributeMap attributes,
	Optional<Boolean> sun,
	Optional<Boolean> moon,
	Optional<Rotation> celestialRotation
) {
	public static final Codec<Atmosphere> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("id").forGetter(Atmosphere::id),
		SkyboxTextureData.CODEC.optionalFieldOf("skybox_texture").forGetter(Atmosphere::skybox),
		EnvironmentAttributeMap.CODEC.optionalFieldOf("attributes", EnvironmentAttributeMap.EMPTY).forGetter(Atmosphere::attributes),
		Codec.BOOL.optionalFieldOf("sun").forGetter(Atmosphere::sun),
		Codec.BOOL.optionalFieldOf("moon").forGetter(Atmosphere::moon),
		Rotation.CODEC.optionalFieldOf("celestial_rotation").forGetter(Atmosphere::celestialRotation)
	).apply(instance, Atmosphere::new));

	public static final RegistryKeys<Atmosphere> REGISTRY_KEYS = RegistryKeys.createKeys(ID.vidlib("atmosphere"));
	public static final VLRegistry<Atmosphere> REGISTRY = VLRegistry.createClient(REGISTRY_KEYS);

	public static final DataType<ResourceKey<Atmosphere>> ID_DATA_TYPE = DataType.of(REGISTRY_KEYS.codec(), REGISTRY_KEYS.streamCodec());

	public static Atmosphere empty(Identifier id) {
		return new Atmosphere(id, Optional.empty(), EnvironmentAttributeMap.EMPTY, Optional.empty(), Optional.empty(), Optional.empty());
	}

	public static class Loader extends JsonRegistryReloadListener<Atmosphere> {
		public Loader() {
			super("vidlib/atmosphere", DIRECT_CODEC, true, REGISTRY);
		}
	}
}
