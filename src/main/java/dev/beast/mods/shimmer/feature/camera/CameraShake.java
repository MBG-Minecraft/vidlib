package dev.beast.mods.shimmer.feature.camera;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.math.Easing;
import dev.beast.mods.shimmer.math.EasingGroup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

@AutoInit
public record CameraShake(
	CameraShakeType type,
	int duration,
	float speed,
	float intensity,
	EasingGroup start,
	EasingGroup end,
	boolean motionBlur
) {
	public static final CameraShake DEFAULT = new CameraShake(
		LemniscateCameraShakeType.DEFAULT.instance(),
		25,
		4F,
		0.6F,
		EasingGroup.QUINT,
		EasingGroup.CUBIC,
		false
	);

	public static final Codec<CameraShake> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		CameraShakeType.REGISTRY.valueCodec().optionalFieldOf("type", DEFAULT.type).forGetter(CameraShake::type),
		Codec.INT.optionalFieldOf("duration", DEFAULT.duration).forGetter(CameraShake::duration),
		Codec.FLOAT.optionalFieldOf("speed", DEFAULT.speed).forGetter(CameraShake::speed),
		Codec.FLOAT.optionalFieldOf("intensity", DEFAULT.intensity).forGetter(CameraShake::intensity),
		EasingGroup.CODEC.optionalFieldOf("start", DEFAULT.start).forGetter(CameraShake::start),
		EasingGroup.CODEC.optionalFieldOf("end", DEFAULT.end).forGetter(CameraShake::end),
		Codec.BOOL.optionalFieldOf("motion_blur", DEFAULT.motionBlur).forGetter(CameraShake::motionBlur)
	).apply(instance, CameraShake::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CameraShake> STREAM_CODEC = CompositeStreamCodec.of(
		CameraShakeType.REGISTRY.valueStreamCodec().optional(DEFAULT.type), CameraShake::type,
		ByteBufCodecs.VAR_INT, CameraShake::duration,
		ByteBufCodecs.FLOAT, CameraShake::speed,
		ByteBufCodecs.FLOAT, CameraShake::intensity,
		EasingGroup.STREAM_CODEC.optional(DEFAULT.start), CameraShake::start,
		EasingGroup.STREAM_CODEC.optional(DEFAULT.end), CameraShake::end,
		ByteBufCodecs.BOOL, CameraShake::motionBlur,
		CameraShake::new
	);

	public static final KnownCodec<CameraShake> KNOWN_CODEC = KnownCodec.register(Shimmer.id("camera_shake"), CODEC, STREAM_CODEC, CameraShake.class);

	public static final ResourceLocation MOTION_BLUR_EFFECT = ResourceLocation.withDefaultNamespace("shaders/post/phosphor.json");

	public CameraShake withIntensityMod(float intensityMod) {
		return new CameraShake(
			type,
			duration,
			speed,
			intensity * intensityMod,
			start,
			end,
			motionBlur
		);
	}

	public CameraShake withDuration(int duration) {
		return new CameraShake(
			type,
			duration,
			speed,
			intensity,
			start,
			end,
			motionBlur
		);
	}

	public CameraShake atDistance(Vec3 camera, Vec3 source, double maxDistance) {
		return withIntensityMod((float) Easing.QUINT_IN.ease(1D - Math.clamp(camera.distanceTo(source) / maxDistance, 0D, 1D)));
	}

	public boolean skip() {
		return intensity <= 0F || speed <= 0F || duration <= 0;
	}
}
