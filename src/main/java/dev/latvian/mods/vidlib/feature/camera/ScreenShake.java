package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.easing.EasingGroup;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public record ScreenShake(
	ScreenShakeType type,
	int duration,
	float speed,
	float intensity,
	EasingGroup start,
	EasingGroup end,
	boolean motionBlur
) {
	public static final ScreenShake NONE = new ScreenShake(
		LemniscateScreenShakeType.DEFAULT.instance(),
		0,
		0F,
		0F,
		EasingGroup.LINEAR,
		EasingGroup.LINEAR,
		false
	);

	public static final ScreenShake DEFAULT = new ScreenShake(
		LemniscateScreenShakeType.DEFAULT.instance(),
		25,
		4F,
		0.6F,
		EasingGroup.QUINT,
		EasingGroup.CUBIC,
		false
	);

	public static final Codec<ScreenShake> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ScreenShakeType.REGISTRY.valueCodec().optionalFieldOf("type", DEFAULT.type).forGetter(ScreenShake::type),
		KLibCodecs.TICKS.optionalFieldOf("duration", DEFAULT.duration).forGetter(ScreenShake::duration),
		Codec.FLOAT.optionalFieldOf("speed", DEFAULT.speed).forGetter(ScreenShake::speed),
		Codec.FLOAT.optionalFieldOf("intensity", DEFAULT.intensity).forGetter(ScreenShake::intensity),
		EasingGroup.CODEC.optionalFieldOf("start", DEFAULT.start).forGetter(ScreenShake::start),
		EasingGroup.CODEC.optionalFieldOf("end", DEFAULT.end).forGetter(ScreenShake::end),
		Codec.BOOL.optionalFieldOf("motion_blur", DEFAULT.motionBlur).forGetter(ScreenShake::motionBlur)
	).apply(instance, ScreenShake::new));

	public static final Codec<ScreenShake> CODEC = Codec.either(Codec.BOOL, DIRECT_CODEC).xmap(either -> either.map(b -> b ? DEFAULT : NONE, Function.identity()), shake -> shake.equals(NONE) ? Either.left(false) : shake.equals(DEFAULT) ? Either.left(true) : Either.right(shake));

	public static final StreamCodec<RegistryFriendlyByteBuf, ScreenShake> STREAM_CODEC = CompositeStreamCodec.of(
		KLibStreamCodecs.optional(ScreenShakeType.REGISTRY.valueStreamCodec(), DEFAULT.type), ScreenShake::type,
		ByteBufCodecs.VAR_INT, ScreenShake::duration,
		ByteBufCodecs.FLOAT, ScreenShake::speed,
		ByteBufCodecs.FLOAT, ScreenShake::intensity,
		KLibStreamCodecs.optional(EasingGroup.STREAM_CODEC, DEFAULT.start), ScreenShake::start,
		KLibStreamCodecs.optional(EasingGroup.STREAM_CODEC, DEFAULT.end), ScreenShake::end,
		ByteBufCodecs.BOOL, ScreenShake::motionBlur,
		ScreenShake::new
	);

	public static final DataType<ScreenShake> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, ScreenShake.class);
	public static final CommandDataType<ScreenShake> COMMAND = CommandDataType.of(DATA_TYPE);

	public static final ResourceLocation MOTION_BLUR_EFFECT = ResourceLocation.withDefaultNamespace("shaders/post/phosphor.json");

	public ScreenShake withIntensityMod(float intensityMod) {
		return new ScreenShake(
			type,
			duration,
			speed,
			intensity * intensityMod,
			start,
			end,
			motionBlur
		);
	}

	public ScreenShake withSpeed(float speed) {
		return new ScreenShake(
			type,
			duration,
			speed,
			intensity,
			start,
			end,
			motionBlur
		);
	}

	public ScreenShake withDuration(int duration) {
		return new ScreenShake(
			type,
			duration,
			speed,
			intensity,
			start,
			end,
			motionBlur
		);
	}

	public ScreenShake atDistance(Vec3 camera, Vec3 source, double maxDistance) {
		return withIntensityMod((float) Easing.QUINT_IN.ease(1D - Math.clamp(camera.distanceTo(source) / maxDistance, 0D, 1D)));
	}

	public boolean skip() {
		return intensity <= 0F || speed <= 0F || duration <= 0;
	}
}
