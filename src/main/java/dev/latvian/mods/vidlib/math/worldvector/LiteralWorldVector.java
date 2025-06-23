package dev.latvian.mods.vidlib.math.worldvector;

import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum LiteralWorldVector implements WorldVector, WorldVectorImBuilder {
	ORIGIN("Origin", "origin", ctx -> ctx.originPos),
	SOURCE("Source", "source", ctx -> ctx.sourcePos),
	TARGET("Target", "target", ctx -> ctx.targetPos);

	public static final LiteralWorldVector[] VALUES = values();
	public static final Map<String, LiteralWorldVector> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(LiteralWorldVector::toString, Function.identity()));

	public final String name;
	public final Function<WorldNumberContext, Vec3> factory;
	public final SimpleRegistryType.Unit<LiteralWorldVector> type;
	public final ImBuilderHolder<WorldVector> builderHolder;

	LiteralWorldVector(String displayName, String name, Function<WorldNumberContext, Vec3> factory) {
		this.name = name;
		this.factory = factory;
		this.type = SimpleRegistryType.unit(name, this);
		this.builderHolder = new ImBuilderHolder<>(displayName, () -> this);
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

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return ImUpdate.NONE;
	}

	@Override
	public WorldVector build() {
		return this;
	}
}
