package dev.beast.mods.shimmer.feature.net;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import dev.beast.mods.shimmer.util.Cast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public record ShimmerPacketType<T extends ShimmerPacketPayload>(CustomPacketPayload.Type<ShimmerPacketPayloadContainer> type, StreamCodec<? super RegistryFriendlyByteBuf, ShimmerPacketPayloadContainer> streamCodec) {
	public static final IPayloadHandler<ShimmerPacketPayloadContainer> HANDLER = (payload, ctx) -> {
		if (payload.wrapped().allowDebugLogging()) {
			if (ShimmerConfig.debugS2CPackets && !(ctx.player() instanceof ServerPlayer)) {
				Shimmer.LOGGER.info("S2C Packet '%s' #%,d @ %,d: %s".formatted(payload.type().id(), payload.uid(), payload.remoteGameTime(), payload.wrapped()));
			}
		}

		payload.wrapped().handleAsync(new ShimmerPayloadContext(ctx, payload.uid(), payload.remoteGameTime()));
	};

	public static <T extends ShimmerPacketPayload> ShimmerPacketType<T> create(ResourceLocation id, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return new ShimmerPacketType<>(new CustomPacketPayload.Type<>(id), new StreamCodec<>() {
			@Override
			public ShimmerPacketPayloadContainer decode(RegistryFriendlyByteBuf buf) {
				long uid = buf.readVarLong();
				long remoteGameTime = buf.readVarLong();
				T payload = streamCodec.decode(buf);
				return new ShimmerPacketPayloadContainer(payload, uid, remoteGameTime);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buf, ShimmerPacketPayloadContainer value) {
				buf.writeVarLong(value.uid());
				buf.writeVarLong(value.remoteGameTime());
				streamCodec.encode(buf, Cast.to(value.wrapped()));
			}
		});
	}

	@ApiStatus.Internal
	public static <T extends ShimmerPacketPayload> ShimmerPacketType<T> internal(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(Shimmer.id(path), streamCodec);
	}

	public static <T extends ShimmerPacketPayload> ShimmerPacketType<T> video(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(ResourceLocation.fromNamespaceAndPath("video", path), streamCodec);
	}
}
