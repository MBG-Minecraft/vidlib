package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class ClientCutscene implements CameraOverride {
	public final Minecraft mc;
	public final boolean overrideCamera;
	public final Cutscene cutscene;
	public final KNumberVariables variables;
	public final Supplier<Vec3> sourcePos;
	public final Vec3 originPos;
	public int prevTotalTick;
	public int totalTick;
	public int totalLength;
	public final CutsceneState state;

	public ClientCutscene(Minecraft mc, boolean overrideCamera, Cutscene cutscene, KNumberVariables variables, Supplier<Vec3> sourcePos) {
		this.mc = mc;
		this.overrideCamera = overrideCamera;
		this.cutscene = cutscene;
		this.variables = variables;
		this.sourcePos = sourcePos;
		this.originPos = sourcePos.get();

		this.prevTotalTick = 0;
		this.totalTick = 0;
		this.totalLength = 0;
		this.state = new CutsceneState();

		var ctx = mc.level.getGlobalContext().fork(variables);
		ctx.sourcePos = originPos;
		ctx.originPos = originPos;

		for (var c : cutscene.steps) {
			this.totalLength = Math.max(totalLength, c.start + c.length);
		}

		ctx.maxTick = (double) totalLength;

		for (var step : cutscene.steps) {
			if (step.start == 0) {
				ctx.progress = 0D;
				ctx.tick = 0D;
				ctx.maxTick = (double) step.length;

				step.start(state, ctx);
			}
		}

		if (state.origin == null) {
			state.origin = ctx.sourcePos;
		}

		if (state.target == null) {
			state.target = ctx.sourcePos.add(mc.player.getViewVector(1F));
		}

		state.snap();
	}

	public boolean tick() {
		prevTotalTick = totalTick;
		state.snap();
		state.topBar.clear();
		state.bottomBar.clear();

		var ctx = mc.level.getGlobalContext().fork(variables);
		ctx.sourcePos = sourcePos.get();
		ctx.originPos = originPos;
		ctx.targetPos = state.target;

		for (var step : cutscene.steps) {
			if (totalTick == step.start + step.length) {
				ctx.progress = 1D;
				ctx.tick = (double) step.length;
				ctx.maxTick = (double) step.length;

				step.exit(state, ctx);
			}
		}

		for (var step : cutscene.steps) {
			if (totalTick >= step.start && totalTick < step.start + step.length) {
				ctx.tick = (double) (totalTick - step.start);
				ctx.maxTick = (double) step.length;
				ctx.progress = ctx.tick / ctx.maxTick;

				step.tick(state, ctx);
			}
		}

		totalTick++;
		return totalTick >= totalLength;
	}

	@Override
	public boolean overrideCamera() {
		return overrideCamera;
	}

	@Override
	public boolean hideGui() {
		return true;
	}

	@Override
	public double getFOVModifier(double delta) {
		return KMath.lerp(delta, state.prevFovMod, state.fovMod);
	}

	@Override
	public boolean renderPlayer() {
		return !cutscene.hidePlayer;
	}

	@Override
	public Vec3 getCameraPosition(float delta) {
		return state.prevOrigin.lerp(state.origin, delta);
	}

	@Override
	public Rotation getCameraRotation(float delta, Vec3 cameraPos) {
		return Rotation.compute(cameraPos, state.prevTarget.lerp(state.target, delta));
	}

	public void stopped() {
		for (var task : state.exitTasks) {
			task.run();
		}

		state.exitTasks.clear();
	}
}
