package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.visual.CubeTextures;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.Set;

public record ZoneVolume(
	ZoneShape shape,
	Color color,
	EntityFilter entityFilter,
	CompoundTag data,
	EntityFilter solid,
	Set<String> tags,
	boolean forceLoaded,
	Optional<ZoneFluid> fluid,
	Optional<CubeTextures> textures,
	ZoneFog fog
) {
	public static final Codec<ZoneVolume> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ZoneShape.CODEC.fieldOf("shape").forGetter(ZoneVolume::shape),
		Color.CODEC.optionalFieldOf("color", Color.CYAN).forGetter(ZoneVolume::color),
		EntityFilter.CODEC.optionalFieldOf("entity_filter", EntityFilter.PLAYER.instance()).forGetter(ZoneVolume::entityFilter),
		CompoundTag.CODEC.optionalFieldOf("data", Empty.COMPOUND_TAG).forGetter(ZoneVolume::data),
		EntityFilter.CODEC.optionalFieldOf("solid", EntityFilter.NONE.instance()).forGetter(ZoneVolume::solid),
		KLibCodecs.setOf(Codec.STRING).optionalFieldOf("tags", Set.of()).forGetter(ZoneVolume::tags),
		Codec.BOOL.optionalFieldOf("force_loaded", false).forGetter(ZoneVolume::forceLoaded),
		ZoneFluid.CODEC.optionalFieldOf("fluid").forGetter(ZoneVolume::fluid),
		CubeTextures.CODEC.optionalFieldOf("textures").forGetter(ZoneVolume::textures),
		ZoneFog.CODEC.optionalFieldOf("fog", ZoneFog.NONE).forGetter(ZoneVolume::fog)
	).apply(instance, ZoneVolume::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ZoneVolume> STREAM_CODEC = CompositeStreamCodec.of(
		ZoneShape.STREAM_CODEC, ZoneVolume::shape,
		Color.STREAM_CODEC, ZoneVolume::color,
		EntityFilter.STREAM_CODEC, ZoneVolume::entityFilter,
		MCStreamCodecs.COMPOUND_TAG, ZoneVolume::data,
		EntityFilter.STREAM_CODEC, ZoneVolume::solid,
		KLibStreamCodecs.linkedSetOf(ByteBufCodecs.STRING_UTF8), ZoneVolume::tags,
		ByteBufCodecs.BOOL, ZoneVolume::forceLoaded,
		ByteBufCodecs.optional(ZoneFluid.STREAM_CODEC), ZoneVolume::fluid,
		CubeTextures.OPTIONAL_STREAM_CODEC, ZoneVolume::textures,
		ZoneFog.STREAM_CODEC, ZoneVolume::fog,
		ZoneVolume::new
	);

	public ZoneVolume(ZoneShape shape, Color color, EntityFilter entityFilter, CompoundTag data) {
		this(
			shape,
			color,
			entityFilter,
			data,
			EntityFilter.NONE.instance(),
			Set.of(),
			false,
			Optional.empty(),
			Optional.empty(),
			ZoneFog.NONE
		);
	}

	public ZoneVolume(ZoneShape shape, Color color) {
		this(
			shape,
			color,
			EntityFilter.PLAYER.instance(),
			Empty.COMPOUND_TAG
		);
	}

	public ZoneVolume withShape(ZoneShape shape) {
		return new ZoneVolume(shape, color, entityFilter, data, solid, tags, forceLoaded, fluid, textures, fog);
	}

	public ZoneVolume withColor(Color color) {
		return new ZoneVolume(shape, color, entityFilter, data, solid, tags, forceLoaded, fluid, textures, fog);
	}

	public boolean isSolid() {
		return solid != EntityFilter.NONE.instance();
	}

	public boolean isVisible() {
		return isSolid() || !fluid.isEmpty() || textures.isPresent() || !fog.isNone();
	}
}
