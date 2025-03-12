package dev.beast.mods.shimmer.feature.camerashake;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.math.EasingGroup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
	public static final Codec<CameraShake> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		CameraShakeType.REGISTRY.valueCodec().optionalFieldOf("type", LemniscateCameraShakeType.DEFAULT.instance()).forGetter(CameraShake::type),
		Codec.INT.optionalFieldOf("duration", 40).forGetter(CameraShake::duration),
		Codec.FLOAT.optionalFieldOf("speed", 5F).forGetter(CameraShake::speed),
		Codec.FLOAT.optionalFieldOf("intensity", 0.3F).forGetter(CameraShake::intensity),
		EasingGroup.CODEC.optionalFieldOf("start", EasingGroup.QUINT).forGetter(CameraShake::start),
		EasingGroup.CODEC.optionalFieldOf("end", EasingGroup.CUBIC).forGetter(CameraShake::end),
		Codec.BOOL.optionalFieldOf("motion_blur", false).forGetter(CameraShake::motionBlur)
	).apply(instance, CameraShake::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CameraShake> STREAM_CODEC = CompositeStreamCodec.of(
		CameraShakeType.REGISTRY.valueStreamCodec(), CameraShake::type,
		ByteBufCodecs.VAR_INT, CameraShake::duration,
		ByteBufCodecs.FLOAT, CameraShake::speed,
		ByteBufCodecs.FLOAT, CameraShake::intensity,
		EasingGroup.STREAM_CODEC, CameraShake::start,
		EasingGroup.STREAM_CODEC, CameraShake::end,
		ByteBufCodecs.BOOL, CameraShake::motionBlur,
		CameraShake::new
	);

	public static final KnownCodec<CameraShake> KNOWN_CODEC = KnownCodec.register(Shimmer.id("camera_shake"), CODEC, CameraShake.class);

	public static float intensity(float intensityMod, Vec3 cameraPos, Vec3 shakeSourcePos) {
		return 1F;
	}
}
