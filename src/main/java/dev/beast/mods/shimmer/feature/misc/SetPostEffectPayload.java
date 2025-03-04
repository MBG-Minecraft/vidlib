package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetPostEffectPayload(ResourceLocation id) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<SetPostEffectPayload> TYPE = ShimmerPacketType.internal("set_post_effect", ResourceLocation.STREAM_CODEC.map(SetPostEffectPayload::new, SetPostEffectPayload::id));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().setPostEffect(id);
	}
}
