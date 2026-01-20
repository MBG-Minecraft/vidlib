package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarLong;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public record VidLibPacketType<T extends SimplePacketPayload>(CustomPacketPayload.Type<VidLibPacketPayloadContainer> type, StreamCodec<ByteBuf, VidLibPacketPayloadContainer> streamCodec) {
	public static <T extends SimplePacketPayload> VidLibPacketType<T> create(ResourceLocation id, StreamCodec<? extends ByteBuf, T> streamCodec) {
		return new VidLibPacketType<>(new CustomPacketPayload.Type<>(id), new StreamCodec<>() {
			@Override
			public VidLibPacketPayloadContainer decode(ByteBuf buf) {
				long uid = VarLong.read(buf);
				long remoteGameTime = VarLong.read(buf);
				T payload = streamCodec.decode(Cast.to(buf));
				return new VidLibPacketPayloadContainer(payload, uid, remoteGameTime);
			}

			@Override
			public void encode(ByteBuf buf, VidLibPacketPayloadContainer value) {
				VarLong.write(buf, value.uid());
				VarLong.write(buf, value.remoteGameTime());
				streamCodec.encode(Cast.to(buf), Cast.to(value.wrapped()));
			}
		});
	}

	@ApiStatus.Internal
	public static <T extends SimplePacketPayload> VidLibPacketType<T> internal(String path, StreamCodec<? extends ByteBuf, T> streamCodec) {
		return create(VidLib.id(path), streamCodec);
	}

	public static <T extends SimplePacketPayload> VidLibPacketType<T> video(String path, StreamCodec<? extends ByteBuf, T> streamCodec) {
		return create(ID.video(path), streamCodec);
	}
}
