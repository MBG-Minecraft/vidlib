package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface WorldVector {
	SimpleRegistry<WorldVector> REGISTRY = SimpleRegistry.create(WorldVector::type);

	static WorldVector named(String name) {
		return switch (name) {
			case "source" -> SourceWorldVector.INSTANCE;
			case "target" -> TargetWorldVector.INSTANCE;
			default -> new VariableWorldVector(name);
		};
	}

	Codec<WorldVector> LITERAL_CODEC = Codec.either(Vec3.CODEC, Codec.STRING).xmap(
		e -> e.map(FixedWorldVector::new, WorldVector::named),
		v -> v instanceof FixedWorldVector(Vec3 pos) ? Either.left(pos) : Either.right(v.toString())
	);

	Codec<WorldVector> VEC3_CODEC = Codec.either(LITERAL_CODEC, REGISTRY.valueCodec()).xmap(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	Codec<WorldVector> CODEC = Codec.either(VEC3_CODEC, WorldNumber.CODEC).xmap(
		e -> e.map(Function.identity(), ScalarWorldVector::new),
		v -> v instanceof ScalarWorldVector(WorldNumber n) ? Either.right(n) : Either.left(v)
	);

	StreamCodec<ByteBuf, WorldVector> LITERAL_STREAM_CODEC = ByteBufCodecs.either(Vec3.STREAM_CODEC, ByteBufCodecs.STRING_UTF8).map(
		e -> e.map(FixedWorldVector::new, WorldVector::named),
		v -> v instanceof FixedWorldVector(Vec3 pos) ? Either.left(pos) : Either.right(v.toString())
	);

	StreamCodec<RegistryFriendlyByteBuf, WorldVector> VEC3_STREAM_CODEC = ByteBufCodecs.either(LITERAL_STREAM_CODEC, REGISTRY.valueStreamCodec()).map(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	StreamCodec<RegistryFriendlyByteBuf, WorldVector> STREAM_CODEC = ByteBufCodecs.either(VEC3_STREAM_CODEC, WorldNumber.STREAM_CODEC).map(
		e -> e.map(Function.identity(), ScalarWorldVector::new),
		v -> v instanceof ScalarWorldVector(WorldNumber n) ? Either.right(n) : Either.left(v)
	);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(FixedWorldVector.ZERO);
		REGISTRY.register(FixedWorldVector.ONE);
		REGISTRY.register(FixedWorldVector.TYPE);
		REGISTRY.register(DynamicWorldVector.TYPE);
		REGISTRY.register(ScalarWorldVector.TYPE);
		REGISTRY.register(OffsetWorldVector.TYPE);
		REGISTRY.register(ScaledWorldVector.TYPE);
		REGISTRY.register(InterpolatedWorldVector.TYPE);
		REGISTRY.register(FollowingEntityWorldVector.TYPE);
		REGISTRY.register(SourceWorldVector.TYPE);
		REGISTRY.register(TargetWorldVector.TYPE);
		REGISTRY.register(VariableWorldVector.TYPE);
		REGISTRY.register(PivotingWorldVector.TYPE);
	}

	static WorldVector fixed(Vec3 position) {
		return new FixedWorldVector(position);
	}

	static WorldVector fixed(double x, double y, double z) {
		return fixed(new Vec3(x, y, z));
	}

	static WorldVector fixed(Vec3i pos) {
		return fixed(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	static WorldVector ofRotation(double yaw, double pitch) {
		var yc = Math.cos(Math.toRadians(-yaw) - Math.PI);
		var ys = Math.sin(Math.toRadians(-yaw) - Math.PI);
		var pc = -Math.cos(Math.toRadians(-pitch));
		var ps = Math.sin(Math.toRadians(-pitch));
		return fixed(ys * pc * 8D, ps * 8D, yc * pc * 8D);
	}

	static WorldVector following(Entity entity, EntityPositionType type) {
		return new FollowingEntityWorldVector(Either.left(entity.getId()), type);
	}

	static WorldVector followingBottomOf(Entity entity) {
		return following(entity, EntityPositionType.BOTTOM);
	}

	static WorldVector followingCenterOf(Entity entity) {
		return following(entity, EntityPositionType.CENTER);
	}

	static WorldVector followingTopOf(Entity entity) {
		return following(entity, EntityPositionType.TOP);
	}

	static WorldVector followingEyesOf(Entity entity) {
		return following(entity, EntityPositionType.EYES);
	}

	static WorldVector followingLeashOf(Entity entity) {
		return following(entity, EntityPositionType.LEASH);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	@Nullable
	Vec3 get(WorldNumberContext ctx);

	default WorldVector offset(WorldVector other) {
		return new OffsetWorldVector(this, other);
	}

	default WorldVector scale(WorldVector other) {
		return new ScaledWorldVector(this, other);
	}

	default WorldVector interpolate(Easing easing, float start, float end, WorldVector other) {
		return new InterpolatedWorldVector(easing, start, end, this, other);
	}

	default WorldVector interpolate(Easing easing, WorldVector other) {
		return interpolate(easing, 0F, 1F, other);
	}

	default boolean isLiteral() {
		return false;
	}
}
