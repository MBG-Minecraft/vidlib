package dev.latvian.mods.vidlib.math.worldvector;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum LiteralWorldVector implements WorldVector {
	ORIGIN("origin", "Origin", ctx -> ctx.originPos),
	SOURCE("source", "Source", ctx -> ctx.sourcePos),
	TARGET("target", "Target", ctx -> ctx.targetPos);

	public static final LiteralWorldVector[] VALUES = values();
	public static final Map<String, LiteralWorldVector> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(LiteralWorldVector::toString, Function.identity()));

	public final String name;
	public final String displayName;
	public final Function<WorldNumberContext, Vec3> factory;
	public final SimpleRegistryType.Unit<LiteralWorldVector> type;

	LiteralWorldVector(String name, String displayName, Function<WorldNumberContext, Vec3> factory) {
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
	public Vec3 get(WorldNumberContext ctx) {
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
