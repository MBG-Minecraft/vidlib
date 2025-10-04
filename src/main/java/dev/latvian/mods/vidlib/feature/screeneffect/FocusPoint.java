package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec2;

import java.util.function.Function;

public sealed interface FocusPoint permits FocusPoint.Screen, FocusPoint.World {
	Codec<FocusPoint> CODEC = Codec.either(Screen.CODEC, World.CODEC).xmap(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v instanceof Screen s ? Either.left(s) : Either.right((World) v)
	);

	StreamCodec<RegistryFriendlyByteBuf, FocusPoint> STREAM_CODEC = ByteBufCodecs.either(Screen.STREAM_CODEC, World.STREAM_CODEC).map(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v instanceof Screen s ? Either.left(s) : Either.right((World) v)
	);

	Vec2 get(KNumberContext ctx);

	record Screen(KNumber x, KNumber y) implements FocusPoint {
		public static final Codec<Screen> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			KNumber.CODEC.fieldOf("x").forGetter(Screen::x),
			KNumber.CODEC.fieldOf("y").forGetter(Screen::y)
		).apply(instance, Screen::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Screen> STREAM_CODEC = CompositeStreamCodec.of(
			KNumber.STREAM_CODEC, Screen::x,
			KNumber.STREAM_CODEC, Screen::y,
			Screen::new
		);

		public static final Screen CENTER = new Screen(KNumber.of(0.5D), KNumber.of(0.5D));

		@Override
		public Vec2 get(KNumberContext ctx) {
			return new Vec2((float) x.getOr(ctx, 0D), (float) y.getOr(ctx, 0D));
		}
	}

	record World(KVector pos) implements FocusPoint {
		public static final Codec<World> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			KVector.CODEC.fieldOf("world").forGetter(World::pos)
		).apply(instance, World::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, World> STREAM_CODEC = CompositeStreamCodec.of(
			KVector.STREAM_CODEC, World::pos,
			World::new
		);

		@Override
		public Vec2 get(KNumberContext ctx) {
			return Vec2.ZERO;
		}
	}
}
