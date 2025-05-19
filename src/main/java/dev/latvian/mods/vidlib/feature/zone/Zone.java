package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.texture.CubeTextures;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.util.Empty;
import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
		VLCodecs.set(Codec.STRING).optionalFieldOf("tags", Set.of()).forGetter(Zone::tags),
		Codec.BOOL.optionalFieldOf("force_loaded", false).forGetter(Zone::forceLoaded),
		ZoneFluid.CODEC.optionalFieldOf("fluid", ZoneFluid.NONE).forGetter(Zone::fluid),
		CubeTextures.CODEC.optionalFieldOf("textures").forGetter(Zone::textures),
		ZoneFog.CODEC.optionalFieldOf("fog", ZoneFog.NONE).forGetter(Zone::fog)
	).apply(instance, Zone::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Zone> STREAM_CODEC = CompositeStreamCodec.of(
		ZoneShape.STREAM_CODEC, Zone::shape,
		Color.STREAM_CODEC, Zone::color,
		EntityFilter.STREAM_CODEC, Zone::entityFilter,
		VLStreamCodecs.COMPOUND_TAG, Zone::data,
		EntityOverride.OVERRIDE_MAP_STREAM_CODEC, Zone::playerOverrides,
		EntityFilter.STREAM_CODEC, Zone::solid,
		ByteBufCodecs.STRING_UTF8.linkedSet(), Zone::tags,
		ByteBufCodecs.BOOL, Zone::forceLoaded,
		ZoneFluid.STREAM_CODEC, Zone::fluid,
		CubeTextures.OPTIONAL_STREAM_CODEC, Zone::textures,
		ZoneFog.STREAM_CODEC, Zone::fog,
		Zone::new
	);

	public void writeUUID(FriendlyByteBuf buf) {
		shape.writeUUID(buf);
		buf.writeInt(color.argb());
		entityFilter.writeUUID(buf);
		VLStreamCodecs.COMPOUND_TAG.encode(buf, data);
		solid.writeUUID(buf);
		buf.writeCollection(tags, ByteBufCodecs.STRING_UTF8);
		buf.writeBoolean(forceLoaded);
		ZoneFluid.STREAM_CODEC.encode(buf, fluid);
		CubeTextures.OPTIONAL_STREAM_CODEC.encode(buf, textures);
		ZoneFog.STREAM_CODEC.encode(buf, fog);
	}

	public UUID computeUUID() {
		var buf = new FriendlyByteBuf(Unpooled.buffer());
		writeUUID(buf);

		try {
			return UUID.nameUUIDFromBytes(buf.array());
		} catch (Exception e) {
			return Util.NIL_UUID;
		}
	}

	public boolean isSolid() {
		return solid != EntityFilter.NONE.instance();
	}

	public boolean isVisible() {
		return isSolid() || !fluid.isEmpty() || textures.isPresent() || !fog.isNone();
	}
}
