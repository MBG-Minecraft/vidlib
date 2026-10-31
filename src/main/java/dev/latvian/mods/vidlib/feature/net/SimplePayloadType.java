package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public record SimplePayloadType<T extends SimplePayload>(CustomPacketPayload.Type<T> type, StreamCodec<ByteBuf, T> streamCodec) {
	public static <T extends SimplePayload> SimplePayloadType<T> create(Identifier id, StreamCodec<? extends ByteBuf, T> streamCodec) {
		return new SimplePayloadType<>(new CustomPacketPayload.Type<>(id), Cast.to(streamCodec));
	}

	@ApiStatus.Internal
	public static <T extends SimplePayload> SimplePayloadType<T> klib(String path, StreamCodec<? extends ByteBuf, T> streamCodec) {
		return create(ID.klib(path), streamCodec);
	}

	@ApiStatus.Internal
	public static <T extends SimplePayload> SimplePayloadType<T> vidlib(String path, StreamCodec<? extends ByteBuf, T> streamCodec) {
		return create(ID.vidlib(path), streamCodec);
	}

	public static <T extends SimplePayload> SimplePayloadType<T> video(String path, StreamCodec<? extends ByteBuf, T> streamCodec) {
		return create(ID.video(path), streamCodec);
	}
}
