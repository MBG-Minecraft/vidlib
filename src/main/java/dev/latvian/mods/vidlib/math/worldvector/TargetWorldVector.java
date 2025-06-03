package dev.latvian.mods.vidlib.math.worldvector;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public enum TargetWorldVector implements WorldVector {
	INSTANCE;

	public static final SimpleRegistryType.Unit<TargetWorldVector> TYPE = SimpleRegistryType.unit("target", INSTANCE);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		return ctx.targetPos;
	}

	@Override
	public String toString() {
		return "target";
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
