package dev.latvian.mods.vidlib.feature.hud;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;

public record PlayTotemOfUndyingPayload(boolean particles, ItemStack itemStack) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<PlayTotemOfUndyingPayload> TYPE = SimplePayloadType.vidlib("play_totem_of_undying", CompositeStreamCodec.of(
		ByteBufCodecs.BOOL, PlayTotemOfUndyingPayload::particles,
		ItemStack.STREAM_CODEC, PlayTotemOfUndyingPayload::itemStack,
		PlayTotemOfUndyingPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var player = ctx.player();
		var mc = Minecraft.getInstance();

		if (particles) {
			mc.particleEngine.createTrackingEmitter(player, ParticleTypes.TOTEM_OF_UNDYING, 30);
		}

		mc.player.displayItemActivation(itemStack);
	}
}
