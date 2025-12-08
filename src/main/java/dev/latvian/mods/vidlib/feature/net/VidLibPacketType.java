package dev.latvian.mods.vidlib.feature.net;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public record VidLibPacketType<T extends SimplePacketPayload>(CustomPacketPayload.Type<VidLibPacketPayloadContainer> type, StreamCodec streamCodec) {
	public static <T extends SimplePacketPayload> VidLibPacketType<T> create(ResourceLocation id, StreamCodec streamCodec) {
		return new VidLibPacketType<>(new CustomPacketPayload.Type<>(id), new StreamCodec() {
			@Override
			public VidLibPacketPayloadContainer decode(Object buf0) {
				var buf = (FriendlyByteBuf) buf0;
				long uid = buf.readVarLong();
				long remoteGameTime = buf.readVarLong();
				T payload = (T) streamCodec.decode(buf);
				return new VidLibPacketPayloadContainer(payload, uid, remoteGameTime);
			}

			@Override
			public void encode(Object buf0, Object value0) {
				var buf = (FriendlyByteBuf) buf0;
				var value = (VidLibPacketPayloadContainer) value0;
				buf.writeVarLong(value.uid());
				buf.writeVarLong(value.remoteGameTime());
				streamCodec.encode(buf, value.wrapped());
			}
		});
	}

	@ApiStatus.Internal
	public static <T extends SimplePacketPayload> VidLibPacketType<T> internal(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(VidLib.id(path), streamCodec);
	}

	public static <T extends SimplePacketPayload> VidLibPacketType<T> video(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(ID.video(path), streamCodec);
	}

	@ApiStatus.Internal
	public static <T extends SimplePacketPayload> VidLibPacketType<T> internalConfig(String path, StreamCodec<? super FriendlyByteBuf, T> streamCodec) {
		return create(VidLib.id(path), streamCodec);
	}

	public static <T extends SimplePacketPayload> VidLibPacketType<T> videoConfig(String path, StreamCodec<? super FriendlyByteBuf, T> streamCodec) {
		return create(ID.video(path), streamCodec);
	}
}
