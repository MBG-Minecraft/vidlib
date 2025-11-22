package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

public record ScreenShakeAtPositionPayload(ScreenShake shake, Vec3 position, double maxDistance) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<ScreenShakeAtPositionPayload> TYPE = VidLibPacketType.internal("screen_shake_at_position", CompositeStreamCodec.of(
		ScreenShake.STREAM_CODEC, ScreenShakeAtPositionPayload::shake,
		MCStreamCodecs.VEC3, ScreenShakeAtPositionPayload::position,
		ByteBufCodecs.DOUBLE, ScreenShakeAtPositionPayload::maxDistance,
		ScreenShakeAtPositionPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().screenShake(shake, position, maxDistance);
	}
}
