package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStepType;
import dev.latvian.mods.vidlib.feature.cutscene.step.state.CutsceneStepState;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientCutscene implements CameraOverride {
	public final Minecraft mc;
	public final Instant start;
	public final boolean overrideCamera;
	public final Cutscene cutscene;
	public final KNumberVariables variables;
	public final Supplier<Vec3> sourcePos;
	public final Vec3 originPos;
	public float prevTotalSecond;
	public float totalSecond;
	public float totalLength;
	public final CutsceneState state;
	public final List<CutsceneStepState> steps;

	public ClientCutscene(Minecraft mc, boolean overrideCamera, Cutscene cutscene, KNumberVariables variables, Supplier<Vec3> sourcePos) {
		this.mc = mc;
		this.start = Instant.now();
		this.overrideCamera = overrideCamera;
		this.cutscene = cutscene;
		this.variables = variables;
		this.sourcePos = sourcePos;
		this.originPos = sourcePos.get();

		this.prevTotalSecond = 0F;
		this.totalSecond = 0F;
		this.totalLength = 0F;
		this.state = new CutsceneState();
		this.steps = new ArrayList<>(cutscene.steps().size());

		var session = ClientLevelSessionData.of(mc.level);

		var ctx = session.createGlobalContext().fork(variables);
		ctx.sourcePos = originPos;
		ctx.originPos = originPos;

		for (var c : cutscene.steps()) {
			var factory = DynamicType.getDynamicTypeProperty(c.type(), CutsceneStepType.STATE_FACTORY, CutsceneStepState.Factory.class);

			if (factory != null && factory.factory().apply(c.type().value()) instanceof CutsceneStepState stepState) {
				this.steps.add(stepState);
				stepState.start = c.start();
				stepState.length = c.length();
				stepState.snap = c.snap();
				this.totalLength = Math.max(totalLength, stepState.start + stepState.length);
			}
		}

		ctx.lifespan = (double) totalLength;

		for (var step : steps) {
			if (step.start == 0F) {
				ctx.time = 0D;
				ctx.lifespan = (double) step.length;
				ctx.progress = 0D;

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
		prevTotalSecond = totalSecond;
		state.snap();
		state.topBar.clear();
		state.bottomBar.clear();

		var session = ClientLevelSessionData.of(mc.level);

		var ctx = session.createGlobalContext().fork(variables);
		ctx.sourcePos = sourcePos.get();
		ctx.originPos = originPos;
		ctx.targetPos = state.target;

		var stepsItr = steps.iterator();

		while (stepsItr.hasNext()) {
			var step = stepsItr.next();

			if (totalSecond >= step.start + step.length) {
				ctx.time = (double) step.length;
				ctx.lifespan = (double) step.length;
				ctx.progress = 1D;

				step.exit(state, ctx);
				stepsItr.remove();
			}
		}

		for (var step : steps) {
			if (totalSecond >= step.start && totalSecond < step.start + step.length) {
				ctx.time = (double) (totalSecond - step.start);
				ctx.lifespan = (double) step.length;
				ctx.progress = ctx.time / ctx.lifespan;

				step.tick(state, ctx);
			}
		}

		var duration = Duration.between(start, Instant.now());
		totalSecond = duration.toSeconds() + duration.toNanosPart() / 1_000_000_000F;
		return totalSecond >= totalLength;
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
	public float getFOVModifier(float delta) {
		return KMath.lerp(delta, state.prevFovMod, state.fovMod);
	}

	@Override
	public boolean renderPlayer() {
		return !cutscene.hidePlayer();
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
