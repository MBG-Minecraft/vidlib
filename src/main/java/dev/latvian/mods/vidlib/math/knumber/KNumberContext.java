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
	public final KNumberVariables variables;

	public Double progress;
	public Double tick;
	public Double maxTick;
	public Level level;
	public DataMap serverDataMap;
	public Double gameTime;
	public Double gameDay;
	public Double clock;
	public Vec3 originPos;
	public Vec3 sourcePos;
	public Vec3 targetPos;

	@ApiStatus.Internal
	public KNumberContext() {
		this.parent = null;
		this.variables = KNumberVariables.EMPTY;

		this.progress = null;
		this.tick = null;
		this.maxTick = null;
		this.level = null;
		this.serverDataMap = null;
		this.gameTime = null;
		this.gameDay = null;
		this.clock = null;
		this.originPos = null;
		this.sourcePos = null;
		this.targetPos = null;
	}

	@ApiStatus.Internal
	public KNumberContext(Level level) {
		this.parent = null;
		this.variables = level.getEnvironment().globalVariables();

		this.progress = null;
		this.tick = null;
		this.maxTick = null;
		this.updateLevelData(level);
		this.originPos = null;
		this.sourcePos = null;
		this.targetPos = null;
	}

	private KNumberContext(KNumberContext parent, @Nullable KNumberVariables variables) {
		this.parent = parent;
		this.variables = variables == null ? KNumberVariables.EMPTY : variables;

		this.progress = parent.progress;
		this.tick = parent.tick;
		this.maxTick = parent.maxTick;
		this.level = parent.level;
		this.serverDataMap = parent.serverDataMap;
		this.gameTime = parent.gameTime;
		this.gameDay = parent.gameDay;
		this.clock = parent.clock;
		this.originPos = parent.originPos;
		this.sourcePos = parent.sourcePos;
		this.targetPos = parent.targetPos;
	}

	public KNumberContext fork(@Nullable KNumberVariables variables) {
		return new KNumberContext(this, variables);
	}

	public void updateLevelData(Level level) {
		this.level = level;

		if (level != null) {
			this.serverDataMap = level.getServerData();
			this.gameTime = (double) level.getGameTime();
			this.gameDay = (double) (level.getGameTime() % 24000L) / 24000D;
			this.clock = (double) level.getDayTime();
		} else {
			this.serverDataMap = null;
			this.gameTime = null;
			this.gameDay = null;
			this.clock = null;
		}
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
