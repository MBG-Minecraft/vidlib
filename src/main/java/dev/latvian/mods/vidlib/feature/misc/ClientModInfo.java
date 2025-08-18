package dev.latvian.mods.vidlib.feature.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ClientModInfo(String id, String name, String version, String fileName) {
	public static final Codec<ClientModInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(ClientModInfo::id),
		Codec.STRING.fieldOf("name").forGetter(ClientModInfo::name),
		Codec.STRING.fieldOf("version").forGetter(ClientModInfo::version),
		Codec.STRING.optionalFieldOf("file_name", "").forGetter(ClientModInfo::fileName)
	).apply(instance, ClientModInfo::new));

	public static final StreamCodec<ByteBuf, ClientModInfo> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, ClientModInfo::id,
		ByteBufCodecs.STRING_UTF8, ClientModInfo::name,
		ByteBufCodecs.STRING_UTF8, ClientModInfo::version,
		ByteBufCodecs.STRING_UTF8, ClientModInfo::fileName,
		ClientModInfo::new
	);
}
