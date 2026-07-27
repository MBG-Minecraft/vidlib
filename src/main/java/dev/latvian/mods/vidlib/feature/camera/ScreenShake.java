package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.interpolation.EaseIn;
import dev.latvian.mods.klib.interpolation.EaseOut;
import dev.latvian.mods.klib.interpolation.FlipXInterpolation;
import dev.latvian.mods.klib.interpolation.JoinedInterpolation;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.EasingType;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public record ScreenShake(
	Ref<ScreenShakeType> type,
	int duration,
	float speed,
	float intensity,
	EasingType ease,
	boolean motionBlur
) {
	public static final ScreenShake NONE = new ScreenShake(
		LemniscateScreenShakeType.DEFAULT,
		0,
		0F,
		0F,
		EasingType.LINEAR,
		false
	);

	public static final ScreenShake DEFAULT = new ScreenShake(
		LemniscateScreenShakeType.DEFAULT,
		25,
		4F,
		0.6F,
		new JoinedInterpolation(EaseOut.ELASTIC.type, new FlipXInterpolation(EaseOut.CUBIC.type).ref()).toEasingType(),
		false
	);

	public static final Codec<ScreenShake> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ScreenShakeType.REGISTRY.codec().optionalFieldOf("type", DEFAULT.type).forGetter(ScreenShake::type),
		KLibCodecs.TICKS.optionalFieldOf("duration", DEFAULT.duration).forGetter(ScreenShake::duration),
		Codec.FLOAT.optionalFieldOf("speed", DEFAULT.speed).forGetter(ScreenShake::speed),
		Codec.FLOAT.optionalFieldOf("intensity", DEFAULT.intensity).forGetter(ScreenShake::intensity),
		EasingType.CODEC.optionalFieldOf("ease", DEFAULT.ease).forGetter(ScreenShake::ease),
		Codec.BOOL.optionalFieldOf("motion_blur", DEFAULT.motionBlur).forGetter(ScreenShake::motionBlur)
	).apply(instance, ScreenShake::new));

	public static final Codec<ScreenShake> CODEC = Codec.either(Codec.BOOL, DIRECT_CODEC).xmap(either -> either.map(b -> b ? DEFAULT : NONE, Function.identity()), shake -> shake.equals(NONE) ? Either.left(false) : shake.equals(DEFAULT) ? Either.left(true) : Either.right(shake));

	public static final StreamCodec<RegistryFriendlyByteBuf, ScreenShake> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.optional(ScreenShakeType.REGISTRY.streamCodec(), DEFAULT.type), ScreenShake::type,
		ByteBufCodecs.VAR_INT, ScreenShake::duration,
		ByteBufCodecs.FLOAT, ScreenShake::speed,
		ByteBufCodecs.FLOAT, ScreenShake::intensity,
		KLibStreamCodecs.optional(MCStreamCodecs.EASING_TYPE, DEFAULT.ease), ScreenShake::ease,
		ByteBufCodecs.BOOL, ScreenShake::motionBlur,
		ScreenShake::new
	);

	public static final DataType<ScreenShake> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC);

	public static final Identifier MOTION_BLUR_EFFECT = Identifier.withDefaultNamespace("shaders/post/phosphor.json");

	public ScreenShake withIntensityMod(float intensityMod) {
		return new ScreenShake(
			type,
			duration,
			speed,
			intensity * intensityMod,
			ease,
			motionBlur
		);
	}

	public ScreenShake withSpeed(float speed) {
		return new ScreenShake(
			type,
			duration,
			speed,
			intensity,
			ease,
			motionBlur
		);
	}

	public ScreenShake withDuration(int duration) {
		return new ScreenShake(
			type,
			duration,
			speed,
			intensity,
			ease,
			motionBlur
		);
	}

	public ScreenShake withInterpolation(EasingType ease) {
		return new ScreenShake(
			type,
			duration,
			speed,
			intensity,
			ease,
			motionBlur
		);
	}

	public ScreenShake atDistance(Vec3 camera, Vec3 source, double maxDistance) {
		return withIntensityMod((float) EaseIn.QUINT.interpolate(1D - Math.clamp(camera.distanceTo(source) / maxDistance, 0D, 1D)));
	}

	public boolean skip() {
		return intensity <= 0F || speed <= 0F || duration <= 0;
	}
}
