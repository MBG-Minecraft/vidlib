package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.IntOrUUID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.ExactEntityFilter;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.FixedKNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.ServerDataKNumber;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

import java.util.function.Function;

public interface KVector extends SimpleRegistryEntry {
	SimpleRegistry<KVector> REGISTRY = SimpleRegistry.create(VidLib.id("kvector"), c -> PlatformHelper.CURRENT.collectKVectors(c));

	FixedKVector ZERO = new FixedKVector(Vec3.ZERO);
	FixedKVector ONE = new FixedKVector(KMath.ONE_VEC3);
	SimpleRegistryType.Unit<FixedKVector> ZERO_TYPE = SimpleRegistryType.unit("zero", ZERO);
	SimpleRegistryType.Unit<FixedKVector> ONE_TYPE = SimpleRegistryType.unit("one", ONE);

	Codec<KVector> LITERAL_CODEC = Codec.either(Vec3.CODEC, Codec.STRING).xmap(
		e -> e.map(FixedKVector::new, KVector::named),
		v -> v instanceof FixedKVector(Vec3 pos) ? Either.left(pos) : Either.right(v.toString())
	);

	Codec<KVector> VEC3_CODEC = Codec.either(LITERAL_CODEC, REGISTRY.codec()).xmap(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	Codec<KVector> CODEC = Codec.either(VEC3_CODEC, KNumber.CODEC).xmap(
		e -> e.map(Function.identity(), ScalarKVector::new),
		v -> v instanceof ScalarKVector(KNumber n) ? Either.right(n) : Either.left(v)
	);

	StreamCodec<ByteBuf, KVector> LITERAL_STREAM_CODEC = ByteBufCodecs.either(MCStreamCodecs.VEC3, ByteBufCodecs.STRING_UTF8).map(
		e -> e.map(FixedKVector::new, KVector::named),
		v -> v instanceof FixedKVector(Vec3 pos) ? Either.left(pos) : Either.right(v.toString())
	);

	StreamCodec<RegistryFriendlyByteBuf, KVector> VEC3_STREAM_CODEC = ByteBufCodecs.either(LITERAL_STREAM_CODEC, REGISTRY.streamCodec()).map(
		e -> e.map(Function.identity(), Function.identity()),
		v -> v.isLiteral() ? Either.left(v) : Either.right(v)
	);

	StreamCodec<RegistryFriendlyByteBuf, KVector> STREAM_CODEC = ByteBufCodecs.either(VEC3_STREAM_CODEC, KNumber.STREAM_CODEC).map(
		e -> e.map(Function.identity(), ScalarKVector::new),
		v -> v instanceof ScalarKVector(KNumber n) ? Either.right(n) : Either.left(v)
	);

	DataType<KVector> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, KVector.class);

	static FixedKVector of(Vec3 vec) {
		if (vec.x == 0D && vec.y == 0D && vec.z == 0D) {
			return ZERO;
		} else if (vec.x == 1D && vec.y == 1D && vec.z == 1D) {
			return ONE;
		} else {
			return new FixedKVector(vec);
		}
	}

	static KVector of(double x, double y, double z) {
		return of(KMath.vec3(x, y, z));
	}

	static KVector of(Vec3i pos) {
		return of(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	}

	static KVector of(Vector3dc pos) {
		return of(pos.x(), pos.y(), pos.z());
	}

	static KVector of(Vector3fc pos) {
		return of(pos.x(), pos.y(), pos.z());
	}

	static KVector named(String name) {
		var v = LiteralKVector.BY_NAME.get(name);

		if (v != null) {
			return v;
		} else if (name.startsWith("$")) {
			var n = new ServerDataKNumber(name.substring(1));
			return new DynamicKVector(n, n, n);
		} else {
			return new VariableKVector(name);
		}
	}

	static void builtinTypes(SimpleRegistryCollector<KVector> registry) {
		registry.register(ZERO_TYPE);
		registry.register(ONE_TYPE);
		registry.register(FixedKVector.TYPE);

		for (var literal : LiteralKVector.values()) {
			registry.register(literal.type);
		}

		registry.register(InterpolatedKVector.TYPE);
		registry.register(DynamicKVector.TYPE);
		registry.register(ScalarKVector.TYPE);
		registry.register(OffsetKVector.TYPE);
		registry.register(ScaledKVector.TYPE);
		registry.register(FollowingEntityKVector.TYPE);
		registry.register(FollowingPropKVector.TYPE);
		registry.register(VariableKVector.TYPE);
		registry.register(IfKVector.TYPE);
		registry.register(PivotingKVector.TYPE);
		registry.register(YRotatedKVector.TYPE);
		registry.register(GroundKVector.TYPE);
	}

	static KVector ofRotation(double yaw, double pitch) {
		var yc = Math.cos(Math.toRadians(-yaw) - Math.PI);
		var ys = Math.sin(Math.toRadians(-yaw) - Math.PI);
		var pc = -Math.cos(Math.toRadians(-pitch));
		var ps = Math.sin(Math.toRadians(-pitch));
		return of(ys * pc * 8D, ps * 8D, yc * pc * 8D);
	}

	static KVector following(EntityFilter entityFilter, PositionType type) {
		return new FollowingEntityKVector(entityFilter, type);
	}

	static KVector following(Entity entity, PositionType type) {
		return following(new ExactEntityFilter(IntOrUUID.of(entity.getId())), type);
	}

	static KVector following(Prop prop, PositionType type) {
		return new FollowingPropKVector(prop.id, type);
	}

	static KVector scalar(KNumber number) {
		if (number instanceof FixedKNumber n) {
			return of(KMath.vec3(n.number()));
		}

		return new ScalarKVector(number);
	}

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	@Nullable
	Vec3 get(KNumberContext ctx);

	default boolean isLiteral() {
		return false;
	}

	default KVector offset(KVector other) {
		return new OffsetKVector(this, other);
	}

	default KVector scale(KVector other) {
		return new ScaledKVector(this, other);
	}
}
