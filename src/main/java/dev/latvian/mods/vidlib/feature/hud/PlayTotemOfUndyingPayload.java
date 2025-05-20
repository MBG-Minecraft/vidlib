package dev.latvian.mods.vidlib.feature.hud;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record PlayTotemOfUndyingPayload(boolean particles, ItemStack itemStack) implements SimplePacketPayload {

	@AutoPacket
	public static final VidLibPacketType<PlayTotemOfUndyingPayload> TYPE = VidLibPacketType.internal("play_totem_of_undying", CompositeStreamCodec.of(
		KnownCodec.BOOL.streamCodec(), PlayTotemOfUndyingPayload::particles,
		ItemStack.STREAM_CODEC, PlayTotemOfUndyingPayload::itemStack,
		PlayTotemOfUndyingPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		Player player = ctx.player();
		Minecraft client = Minecraft.getInstance();
		if (particles) {
			client.particleEngine.createTrackingEmitter(player, ParticleTypes.TOTEM_OF_UNDYING, 30);
		}
		Minecraft.getInstance().gameRenderer.displayItemActivation(itemStack);
	}
}
