package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

public record ScreenShakeAtPositionPayload(ScreenShake shake, Vec3 position, double maxDistance) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<ScreenShakeAtPositionPayload> TYPE = SimplePayloadType.vidlib("screen_shake_at_position", CompositeStreamCodec.of(
		ScreenShake.STREAM_CODEC, ScreenShakeAtPositionPayload::shake,
		MCStreamCodecs.VEC3, ScreenShakeAtPositionPayload::position,
		ByteBufCodecs.DOUBLE, ScreenShakeAtPositionPayload::maxDistance,
		ScreenShakeAtPositionPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().screenShake(shake, position, maxDistance);
	}
}
