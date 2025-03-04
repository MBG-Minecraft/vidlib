package dev.beast.mods.shimmer.feature.net;

import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

public record ShimmerPacketType<T extends ShimmerPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
	public static final IPayloadHandler<ShimmerPacketPayload> HANDLER = ShimmerPacketPayload::handleAsync;

	public static <T extends ShimmerPacketPayload> ShimmerPacketType<T> create(ResourceLocation id, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return new ShimmerPacketType<>(new CustomPacketPayload.Type<T>(id), streamCodec);
	}

	@ApiStatus.Internal
	public static <T extends ShimmerPacketPayload> ShimmerPacketType<T> internal(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(Shimmer.id(path), streamCodec);
	}

	public static <T extends ShimmerPacketPayload> ShimmerPacketType<T> video(String path, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return create(ResourceLocation.fromNamespaceAndPath("video", path), streamCodec);
	}
}
