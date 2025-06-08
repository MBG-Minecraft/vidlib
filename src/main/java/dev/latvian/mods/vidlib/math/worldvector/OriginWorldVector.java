package dev.latvian.mods.vidlib.math.worldvector;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public enum OriginWorldVector implements WorldVector {
	INSTANCE;

	public static final SimpleRegistryType.Unit<OriginWorldVector> TYPE = SimpleRegistryType.unit("origin", OriginWorldVector.INSTANCE);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		return ctx.originPos;
	}

	@Override
	public String toString() {
		return "origin";
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
