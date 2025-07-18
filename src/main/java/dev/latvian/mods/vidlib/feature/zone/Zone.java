package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.visual.CubeTextures;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record Zone(
	ZoneShape shape,
	Color color,
	EntityFilter entityFilter,
	CompoundTag data,
	Map<EntityOverride<?>, Object> playerOverrides,
	EntityFilter solid,
	Set<String> tags,
	boolean forceLoaded,
	ZoneFluid fluid,
	Optional<CubeTextures> textures,
	ZoneFog fog
) {
	public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ZoneShape.CODEC.fieldOf("shape").forGetter(Zone::shape),
		Color.CODEC.optionalFieldOf("color", Color.CYAN).forGetter(Zone::color),
		EntityFilter.CODEC.optionalFieldOf("entity_filter", EntityFilter.PLAYER.instance()).forGetter(Zone::entityFilter),
		CompoundTag.CODEC.optionalFieldOf("data", Empty.COMPOUND_TAG).forGetter(Zone::data),
		EntityOverride.OVERRIDE_MAP_CODEC.optionalFieldOf("player_overrides", Map.of()).forGetter(Zone::playerOverrides),
		EntityFilter.CODEC.optionalFieldOf("solid", EntityFilter.NONE.instance()).forGetter(Zone::solid),
		KLibCodecs.setOf(Codec.STRING).optionalFieldOf("tags", Set.of()).forGetter(Zone::tags),
		Codec.BOOL.optionalFieldOf("force_loaded", false).forGetter(Zone::forceLoaded),
		ZoneFluid.CODEC.optionalFieldOf("fluid", ZoneFluid.NONE).forGetter(Zone::fluid),
		CubeTextures.CODEC.optionalFieldOf("textures").forGetter(Zone::textures),
		ZoneFog.CODEC.optionalFieldOf("fog", ZoneFog.NONE).forGetter(Zone::fog)
	).apply(instance, Zone::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Zone> STREAM_CODEC = CompositeStreamCodec.of(
		ZoneShape.STREAM_CODEC, Zone::shape,
		Color.STREAM_CODEC, Zone::color,
		EntityFilter.STREAM_CODEC, Zone::entityFilter,
		MCStreamCodecs.COMPOUND_TAG, Zone::data,
		EntityOverride.OVERRIDE_MAP_STREAM_CODEC, Zone::playerOverrides,
		EntityFilter.STREAM_CODEC, Zone::solid,
		ByteBufCodecs.STRING_UTF8.linkedSet(), Zone::tags,
		ByteBufCodecs.BOOL, Zone::forceLoaded,
		ZoneFluid.STREAM_CODEC, Zone::fluid,
		CubeTextures.OPTIONAL_STREAM_CODEC, Zone::textures,
		ZoneFog.STREAM_CODEC, Zone::fog,
		Zone::new
	);

	public Zone(ZoneShape shape, Color color, EntityFilter entityFilter, CompoundTag data) {
		this(
			shape,
			color,
			entityFilter,
			data,
			Map.of(),
			EntityFilter.NONE.instance(),
			Set.of(),
			false,
			ZoneFluid.NONE,
			Optional.empty(),
			ZoneFog.NONE
		);
	}

	public Zone(ZoneShape shape, Color color) {
		this(
			shape,
			color,
			EntityFilter.PLAYER.instance(),
			Empty.COMPOUND_TAG
		);
	}

	public Zone withShape(ZoneShape shape) {
		return new Zone(shape, color, entityFilter, data, playerOverrides, solid, tags, forceLoaded, fluid, textures, fog);
	}

	public Zone withColor(Color color) {
		return new Zone(shape, color, entityFilter, data, playerOverrides, solid, tags, forceLoaded, fluid, textures, fog);
	}

	public boolean isSolid() {
		return solid != EntityFilter.NONE.instance();
	}

	public boolean isVisible() {
		return isSolid() || !fluid.isEmpty() || textures.isPresent() || !fog.isNone();
	}
}
