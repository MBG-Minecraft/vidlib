package dev.latvian.mods.vidlib.math.kvector;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum LiteralKVector implements KVector {
	ORIGIN("origin", "Origin", ctx -> ctx.originPos),
	SOURCE("source", "Source", ctx -> ctx.sourcePos),
	TARGET("target", "Target", ctx -> ctx.targetPos);

	public static final LiteralKVector[] VALUES = values();
	public static final Map<String, LiteralKVector> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(LiteralKVector::toString, Function.identity()));

	public final String name;
	public final String displayName;
	public final Function<KNumberContext, Vec3> factory;
	public final SimpleRegistryType.Unit<LiteralKVector> type;

	LiteralKVector(String name, String displayName, Function<KNumberContext, Vec3> factory) {
		this.name = name;
		this.displayName = displayName;
		this.factory = factory;
		this.type = SimpleRegistryType.unit(name, this);
	}

	@Override
	public SimpleRegistryType<?> type() {
		return type;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		return factory.apply(ctx);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
