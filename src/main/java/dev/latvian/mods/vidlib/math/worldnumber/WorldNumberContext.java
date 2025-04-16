package dev.latvian.mods.vidlib.math.worldnumber;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WorldNumberContext {
	public final Level level;
	public final float progress;
	public final WorldNumberVariables variables;
	public Vec3 sourcePos;
	public Vec3 targetPos;

	public WorldNumberContext(Level level, float progress, WorldNumberVariables variables) {
		this.level = level;
		this.progress = progress;
		this.variables = variables;
		this.sourcePos = null;
		this.targetPos = null;
	}
}
