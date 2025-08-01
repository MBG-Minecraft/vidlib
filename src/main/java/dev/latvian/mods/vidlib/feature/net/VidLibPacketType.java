package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public record VidLibPacketType<T extends SimplePacketPayload>(CustomPacketPayload.Type<VidLibPacketPayloadContainer> type, StreamCodec<? super RegistryFriendlyByteBuf, VidLibPacketPayloadContainer> streamCodec) {
	public static <T extends SimplePacketPayload> VidLibPacketType<T> create(ResourceLocation id, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return new VidLibPacketType<>(new CustomPacketPayload.Type<>(id), new StreamCodec<>() {
			@Override
			public VidLibPacketPayloadContainer decode(RegistryFriendlyByteBuf buf) {
				long uid = buf.readVarLong();
				long remoteGameTime = buf.readVarLong();
				T payload = streamCodec.decode(buf);
				return new VidLibPacketPayloadContainer(payload, uid, remoteGameTime);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, VidLibPacketPayloadContainer value) {
				buf.writeVarLong(value.uid());
				buf.writeVarLong(value.remoteGameTime());
				streamCodec.encode(buf, Cast.to(value.wrapped()));
			}
		});
	}

	@ApiStatus.Internal
	public static <T extends SimplePacketPayload> VidLibPacketType<T> internal(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(VidLib.id(path), streamCodec);
	}

	public static <T extends SimplePacketPayload> VidLibPacketType<T> video(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(ResourceLocation.fromNamespaceAndPath("video", path), streamCodec);
	}
}
