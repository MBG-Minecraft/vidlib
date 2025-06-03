package dev.latvian.mods.vidlib.math.worldvector;

import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public enum SourceWorldVector implements WorldVector {
	INSTANCE;

	public static final SimpleRegistryType.Unit<SourceWorldVector> TYPE = SimpleRegistryType.unit("source", SourceWorldVector.INSTANCE);

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		return ctx.sourcePos;
	}

	@Override
	public String toString() {
		return "source";
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
