package dev.latvian.mods.vidlib.feature.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PlatformModInfo(String id, String name, String version, String fileName) {
	public static final Codec<PlatformModInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(PlatformModInfo::id),
		Codec.STRING.fieldOf("name").forGetter(PlatformModInfo::name),
		Codec.STRING.fieldOf("version").forGetter(PlatformModInfo::version),
		Codec.STRING.optionalFieldOf("file_name", "").forGetter(PlatformModInfo::fileName)
	).apply(instance, PlatformModInfo::new));

	public static final StreamCodec<ByteBuf, PlatformModInfo> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, PlatformModInfo::id,
		ByteBufCodecs.STRING_UTF8, PlatformModInfo::name,
		ByteBufCodecs.STRING_UTF8, PlatformModInfo::version,
		ByteBufCodecs.STRING_UTF8, PlatformModInfo::fileName,
		PlatformModInfo::new
	);
}
