package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SkyboxData(
	Identifier id,
	Optional<Identifier> texture,
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
		ID.CODEC.optionalFieldOf("texture").forGetter(SkyboxData::texture),
		Codec.FLOAT.optionalFieldOf("rotation", 0F).forGetter(SkyboxData::rotation),
		Codec.FLOAT.optionalFieldOf("rotating", 0F).forGetter(SkyboxData::rotating),
		Color.CODEC.optionalFieldOf("tint", Color.WHITE).forGetter(SkyboxData::tint),
		Codec.BOOL.optionalFieldOf("celestials", false).forGetter(SkyboxData::celestials),
		Codec.BOOL.optionalFieldOf("sun", true).forGetter(SkyboxData::sun),
		Codec.BOOL.optionalFieldOf("moon", true).forGetter(SkyboxData::moon),
		Codec.FLOAT.optionalFieldOf("stars").forGetter(SkyboxData::stars),
		Rotation.CODEC.optionalFieldOf("celestial_rotation").forGetter(SkyboxData::celestialRotation)
	).apply(instance, SkyboxData::new));

	public static final VLRegistry<SkyboxData> REGISTRY = VLRegistry.createClient("skybox", SkyboxData.class);

	public static final List<Identifier> SKYBOX_IDS = new ArrayList<>();
	public static final DataType<Identifier> ID_DATA_TYPE = DataType.of(ID.CODEC, ID.STREAM_CODEC, Identifier.class);
	public static final CommandDataType<Identifier> COMMAND = CommandDataType.of(ID_DATA_TYPE).suggestsIDs(() -> SKYBOX_IDS);

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
