package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.ShimmerNet;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetPostEffectPayload(ResourceLocation id) implements CustomPacketPayload {
	public static final Type<SetPostEffectPayload> TYPE = ShimmerNet.type("set_post_effect");
	public static final StreamCodec<ByteBuf, SetPostEffectPayload> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(SetPostEffectPayload::new, SetPostEffectPayload::id);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ctx.player().setPostEffect(id));
	}
}
