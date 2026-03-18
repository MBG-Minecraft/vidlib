package dev.latvian.mods.vidlib.feature.maptextureoverride;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MapTextureOverride(int mapId, SpriteKey sprite) {
	public static final Codec<MapTextureOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.fieldOf("map_id").forGetter(MapTextureOverride::mapId),
		SpriteKey.PREFER_SPECIAL_CODEC.fieldOf("sprite").forGetter(MapTextureOverride::sprite)
	).apply(instance, MapTextureOverride::new));

	public static final StreamCodec<ByteBuf, MapTextureOverride> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, MapTextureOverride::mapId,
		SpriteKey.STREAM_CODEC, MapTextureOverride::sprite,
		MapTextureOverride::new
	);
}