package dev.latvian.mods.vidlib.math.worldnumber;

import dev.latvian.mods.vidlib.feature.data.DataMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WorldNumberContext {
	public final Level level;
	public final float progress;
	public WorldNumberVariables variables;
	public DataMap serverDataMap;
	public Vec3 originPos;
	public Vec3 sourcePos;
	public Vec3 targetPos;

	public WorldNumberContext(Level level, float progress, WorldNumberVariables variables) {
		this.level = level;
		this.progress = progress;
		this.variables = variables;
		this.serverDataMap = null;
		this.originPos = null;
		this.sourcePos = null;
		this.targetPos = null;
	}

	public WorldNumberContext withVariables(WorldNumberVariables merge) {
		variables = variables.merge(merge);
		return this;
	}
}
