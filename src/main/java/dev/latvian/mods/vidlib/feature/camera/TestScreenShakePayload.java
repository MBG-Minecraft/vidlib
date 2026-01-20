package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.particle.ShapeParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record TestScreenShakePayload(ScreenShake shake, Optional<Vec3> position, double maxDistance) implements SimplePacketPayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final VidLibPacketType<TestScreenShakePayload> TYPE = VidLibPacketType.internal("screen_shake/test", CompositeStreamCodec.of(
		ScreenShake.STREAM_CODEC, TestScreenShakePayload::shake,
		ByteBufCodecs.optional(MCStreamCodecs.VEC3), TestScreenShakePayload::position,
		ByteBufCodecs.DOUBLE, TestScreenShakePayload::maxDistance,
		TestScreenShakePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			if (position.isPresent()) {
				ctx.level().screenShake(shake, position.get(), maxDistance);
				ctx.level().cubeParticles(new ShapeParticleOptions(ScreenShake.DEFAULT.duration(), Color.CYAN, Color.WHITE), List.of(BlockPos.containing(position.get())));
			} else {
				ctx.level().screenShake(shake);
			}
		}
	}
}
