package dev.beast.mods.shimmer.math.worldnumber;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WorldNumberContext {
	public final Level level;
	public final float progress;
	public Vec3 targetPos;
	public Vec3 sourcePos;

	public WorldNumberContext(Level level, float progress) {
		this.level = level;
		this.progress = progress;
	}
}
