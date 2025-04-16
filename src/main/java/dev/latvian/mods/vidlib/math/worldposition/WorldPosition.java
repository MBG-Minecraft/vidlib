package dev.latvian.mods.vidlib.math.worldposition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kmath.easing.Easing;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface WorldPosition {
	SimpleRegistry<WorldPosition> REGISTRY = SimpleRegistry.create(WorldPosition::type);
	Codec<WorldPosition> CODEC = Codec.either(VLCodecs.VEC_3, REGISTRY.valueCodec()).xmap(either -> either.map(WorldPosition::fixed, Function.identity()), p -> p instanceof FixedWorldPosition(Vec3 pos) ? Either.left(pos) : Either.right(p));
	StreamCodec<RegistryFriendlyByteBuf, WorldPosition> STREAM_CODEC = ByteBufCodecs.either(VLStreamCodecs.VEC_3, REGISTRY.valueStreamCodec()).map(either -> either.map(WorldPosition::fixed, Function.identity()), p -> p instanceof FixedWorldPosition(Vec3 pos) ? Either.left(pos) : Either.right(p));

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(FixedWorldPosition.ZERO);
		REGISTRY.register(FixedWorldPosition.TYPE);
		REGISTRY.register(OffsetWorldPosition.TYPE);
		REGISTRY.register(ScaledWorldPosition.TYPE);
		REGISTRY.register(InterpolatedWorldPosition.TYPE);
		REGISTRY.register(FollowingEntityWorldPosition.TYPE);
		REGISTRY.register(SourceWorldPosition.TYPE);
		REGISTRY.register(TargetWorldPosition.TYPE);
		REGISTRY.register(VariableWorldPosition.TYPE);
		REGISTRY.register(PivotingWorldPosition.TYPE);
	}

	static WorldPosition fixed(Vec3 position) {
		return new FixedWorldPosition(position);
	}

	static WorldPosition fixed(double x, double y, double z) {
		return fixed(new Vec3(x, y, z));
	}

	static WorldPosition fixed(Vec3i pos) {
		return fixed(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	static WorldPosition ofRotation(double yaw, double pitch) {
		var yc = Math.cos(Math.toRadians(-yaw) - Math.PI);
		var ys = Math.sin(Math.toRadians(-yaw) - Math.PI);
		var pc = -Math.cos(Math.toRadians(-pitch));
		var ps = Math.sin(Math.toRadians(-pitch));
		return fixed(ys * pc * 8D, ps * 8D, yc * pc * 8D);
	}

	static WorldPosition following(Entity entity, EntityPositionType type) {
		return new FollowingEntityWorldPosition(Either.left(entity.getId()), type);
	}

	static WorldPosition followingBottomOf(Entity entity) {
		return following(entity, EntityPositionType.BOTTOM);
	}

	static WorldPosition followingCenterOf(Entity entity) {
		return following(entity, EntityPositionType.CENTER);
	}

	static WorldPosition followingTopOf(Entity entity) {
		return following(entity, EntityPositionType.TOP);
	}

	static WorldPosition followingEyesOf(Entity entity) {
		return following(entity, EntityPositionType.EYES);
	}

	static WorldPosition followingLeashOf(Entity entity) {
		return following(entity, EntityPositionType.LEASH);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	@Nullable
	Vec3 get(WorldNumberContext ctx);

	default WorldPosition offset(WorldPosition other) {
		return new OffsetWorldPosition(this, other);
	}

	default WorldPosition scale(WorldPosition other) {
		return new ScaledWorldPosition(this, other);
	}

	default WorldPosition interpolate(Easing easing, float start, float end, WorldPosition other) {
		return new InterpolatedWorldPosition(easing, start, end, this, other);
	}

	default WorldPosition interpolate(Easing easing, WorldPosition other) {
		return interpolate(easing, 0F, 1F, other);
	}
}
