package dev.latvian.mods.vidlib.math.worldposition;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TargetWorldPosition implements WorldPosition {
	public static final SimpleRegistryType.Unit<TargetWorldPosition> TYPE = SimpleRegistryType.unit(VidLib.id("target"), new TargetWorldPosition());

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		return ctx.targetPos;
	}
}
