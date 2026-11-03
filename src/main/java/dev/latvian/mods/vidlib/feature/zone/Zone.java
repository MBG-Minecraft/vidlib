package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.filter.EntityFilter;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.texture.CubeTextures;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.volume.Volume;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.Set;

public record Zone(
	Ref<Volume> volume,
	Color color,
	Ref<EntityFilter> entityFilter,
	CompoundTag data,
	Ref<EntityFilter> solid,
	Set<String> tags,
	boolean forceLoaded,
	Optional<ZoneFluid> fluid,
	Optional<CubeTextures> textures,
	ZoneFog fog
) {
	public static final Codec<Zone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Volume.CODEC.fieldOf("volume").forGetter(Zone::volume),
		Color.CODEC.optionalFieldOf("color", Color.CYAN).forGetter(Zone::color),
		EntityFilter.CODEC.optionalFieldOf("entity_filter", EntityFilter.PLAYER).forGetter(Zone::entityFilter),
		CompoundTag.CODEC.optionalFieldOf("data", Empty.COMPOUND_TAG).forGetter(Zone::data),
		EntityFilter.CODEC.optionalFieldOf("solid", EntityFilter.NONE).forGetter(Zone::solid),
		KLibCodecs.setOf(Codec.STRING).optionalFieldOf("tags", Set.of()).forGetter(Zone::tags),
		Codec.BOOL.optionalFieldOf("force_loaded", false).forGetter(Zone::forceLoaded),
		ZoneFluid.CODEC.optionalFieldOf("fluid").forGetter(Zone::fluid),
		CubeTextures.CODEC.optionalFieldOf("textures").forGetter(Zone::textures),
		ZoneFog.CODEC.optionalFieldOf("fog", ZoneFog.NONE).forGetter(Zone::fog)
	).apply(instance, Zone::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Zone> STREAM_CODEC = CompositeStreamCodec.of(
		Volume.STREAM_CODEC, Zone::volume,
		Color.STREAM_CODEC, Zone::color,
		EntityFilter.STREAM_CODEC, Zone::entityFilter,
		MCStreamCodecs.COMPOUND_TAG, Zone::data,
		KLibStreamCodecs.optional(EntityFilter.STREAM_CODEC, EntityFilter.NONE), Zone::solid,
		KLibStreamCodecs.linkedSetOf(ByteBufCodecs.STRING_UTF8), Zone::tags,
		ByteBufCodecs.BOOL, Zone::forceLoaded,
		ByteBufCodecs.optional(ZoneFluid.STREAM_CODEC), Zone::fluid,
		ByteBufCodecs.optional(CubeTextures.STREAM_CODEC), Zone::textures,
		ZoneFog.STREAM_CODEC, Zone::fog,
		Zone::new
	);

	public Zone(Ref<Volume> shape, Color color, Ref<EntityFilter> entityFilter, CompoundTag data) {
		this(
			shape,
			color,
			entityFilter,
			data,
			EntityFilter.NONE,
			Set.of(),
			false,
			Optional.empty(),
			Optional.empty(),
			ZoneFog.NONE
		);
	}

	public Zone(Ref<Volume> shape, Color color) {
		this(
			shape,
			color,
			EntityFilter.PLAYER,
			Empty.COMPOUND_TAG
		);
	}

	public Zone withShape(Ref<Volume> shape) {
		return new Zone(shape, color, entityFilter, data, solid, tags, forceLoaded, fluid, textures, fog);
	}

	public Zone withColor(Color color) {
		return new Zone(volume, color, entityFilter, data, solid, tags, forceLoaded, fluid, textures, fog);
	}

	public boolean isSolid() {
		return solid != EntityFilter.NONE;
	}

	public boolean isVisible() {
		return isSolid() || !fluid.isEmpty() || textures.isPresent() || !fog.isNone();
	}
}
