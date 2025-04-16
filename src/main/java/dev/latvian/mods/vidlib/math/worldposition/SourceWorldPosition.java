package dev.latvian.mods.vidlib.math.worldposition;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SourceWorldPosition implements WorldPosition {
	public static final SimpleRegistryType.Unit<SourceWorldPosition> TYPE = SimpleRegistryType.unit(VidLib.id("source"), new SourceWorldPosition());

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		return ctx.sourcePos;
	}
}
