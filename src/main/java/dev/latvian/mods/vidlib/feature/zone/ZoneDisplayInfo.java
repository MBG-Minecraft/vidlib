package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.CubeTexturePatch;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ZoneDisplayInfo(
	Color color,
	CubeTexturePatch textures,
	ZoneFog fog
) {
	public static final ZoneDisplayInfo DEFAULT = new ZoneDisplayInfo(
		Color.CYAN,
		CubeTexturePatch.EMPTY,
		ZoneFog.NONE
	);

	public static final Codec<ZoneDisplayInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Color.SOLID_CODEC.optionalFieldOf("color", Color.CYAN).forGetter(ZoneDisplayInfo::color),
		CubeTexturePatch.CODEC.optionalFieldOf("textures", CubeTexturePatch.EMPTY).forGetter(ZoneDisplayInfo::textures),
		ZoneFog.CODEC.optionalFieldOf("fog", ZoneFog.NONE).forGetter(ZoneDisplayInfo::fog)
	).apply(instance, ZoneDisplayInfo::new));

	public static final StreamCodec<ByteBuf, ZoneDisplayInfo> STREAM_CODEC = CompositeStreamCodec.of(
		Color.STREAM_CODEC, ZoneDisplayInfo::color,
		KLibStreamCodecs.optional(CubeTexturePatch.STREAM_CODEC, CubeTexturePatch.EMPTY), ZoneDisplayInfo::textures,
		ZoneFog.STREAM_CODEC, ZoneDisplayInfo::fog,
		ZoneDisplayInfo::new
	);
}
