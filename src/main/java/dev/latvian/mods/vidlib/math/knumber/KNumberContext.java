package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class KNumberContext {
	public final KNumberContext parent;
	public final Level level;
	public final float progress;
	public final KNumberVariables variables;
	private DataMap serverDataMap;
	public Vec3 originPos;
	public Vec3 sourcePos;
	public Vec3 targetPos;

	@ApiStatus.Internal
	public KNumberContext(@Nullable Level level) {
		this.parent = null;
		this.level = level;
		this.progress = 1F;
		this.variables = level == null ? KNumberVariables.EMPTY : level.getEnvironment().globalVariables();
		this.serverDataMap = null;
		this.originPos = null;
		this.sourcePos = null;
		this.targetPos = null;
	}

	private KNumberContext(KNumberContext parent, float progress, @Nullable KNumberVariables variables) {
		this.parent = parent;
		this.level = parent.level;
		this.progress = progress;
		this.variables = variables == null ? KNumberVariables.EMPTY : variables;
		this.serverDataMap = parent.serverDataMap;
		this.originPos = parent.originPos;
		this.sourcePos = parent.sourcePos;
		this.targetPos = parent.targetPos;
	}

	public KNumberContext fork(float progress, @Nullable KNumberVariables variables) {
		return new KNumberContext(this, progress, variables);
	}

	@Nullable
	public KNumber getNumber(String name) {
		var ctx = this;
		KNumber v;

		do {
			v = ctx.variables == null ? null : ctx.variables.numbers().get(name);
			ctx = ctx.parent;
		}
		while (v == null && ctx != null);

		return v;
	}

	@Nullable
	public KVector getVector(String name) {
		var ctx = this;
		KVector v;

		do {
			v = ctx.variables.vectors().get(name);
			ctx = ctx.parent;
		}
		while (v == null && ctx != null);

		return v;
	}

	@Nullable
	public Object getServerData(DataKey<?> key) {
		if (level == null) {
			return null;
		} else if (serverDataMap == null) {
			serverDataMap = level.getServerData();
		}

		return serverDataMap.get(key);
	}
}
