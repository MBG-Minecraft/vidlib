package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.particle.ShapeParticleOptions;
import dev.latvian.mods.vidlib.feature.VLFeatures;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record TestScreenShakePayload(ScreenShake shake, Optional<Vec3> position, double maxDistance) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<TestScreenShakePayload> TYPE = SimplePayloadType.vidlib("screen_shake/test", CompositeStreamCodec.of(
		ScreenShake.STREAM_CODEC, TestScreenShakePayload::shake,
		ByteBufCodecs.optional(MCStreamCodecs.VEC3), TestScreenShakePayload::position,
		ByteBufCodecs.DOUBLE, TestScreenShakePayload::maxDistance,
		TestScreenShakePayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			var features = VLFeatures.of(ctx.level());

			if (position.isPresent()) {
				features.screenShake(shake, position.get(), maxDistance);
				features.shapeParticles(new ShapeParticleOptions(ScreenShake.DEFAULT.duration(), Gradient.CYAN, Gradient.WHITE), List.of(BlockPos.containing(position.get())));
			} else {
				features.screenShake(shake);
			}
		}
	}
}
