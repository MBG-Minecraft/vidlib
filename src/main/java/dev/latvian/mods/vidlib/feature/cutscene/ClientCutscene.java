package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.Rotation;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientCutscene implements CameraOverride {
	public final Minecraft mc;
	public final boolean overrideCamera;
	public final Cutscene cutscene;
	public final WorldNumberVariables variables;
	public final CutsceneStep[] steps;
	public final Supplier<Vec3> sourcePos;
	public final Vec3 originPos;
	public int prevTotalTick;
	public int totalTick;
	public int totalLength;

	public Vec3 prevOrigin, origin, prevTarget, target;
	public double prevFovMod, fovMod;
	public List<FormattedCharSequence> topBar, bottomBar;
	public final List<SoundInstance> playingSounds;

	public ClientCutscene(Minecraft mc, boolean overrideCamera, Cutscene cutscene, WorldNumberVariables variables, Supplier<Vec3> sourcePos) {
		this.mc = mc;
		this.overrideCamera = overrideCamera;
		this.cutscene = cutscene;
		this.variables = variables;
		this.steps = cutscene.steps.toArray(new CutsceneStep[0]);
		this.sourcePos = sourcePos;
		this.originPos = sourcePos.get();

		this.prevTotalTick = 0;
		this.totalTick = 0;
		this.totalLength = 0;
		this.fovMod = 1D;
		this.playingSounds = new ArrayList<>();

		var ctx = new WorldNumberContext(mc.level, 0F, variables);
		ctx.originPos = originPos;
		ctx.sourcePos = originPos;

		for (var step : steps) {
			this.totalLength = Math.max(totalLength, step.resolvedStart + step.resolvedLength);

			if (step.resolvedStart == 0) {
				if (step.target.isPresent()) {
					var t = step.target.get().get(ctx);

					if (target == null) {
						target = t;
						step.prevRenderTarget = step.renderTarget = t;
					}
				}

				ctx.targetPos = target;

				if (origin == null && step.origin.isPresent()) {
					origin = step.origin.get().get(ctx);
				}

				if (step.fovModifier.isPresent()) {
					fovMod = step.fovModifier.get().get(ctx);
				}
			}
		}

		if (origin == null) {
			origin = ctx.sourcePos;
		}

		if (target == null) {
			target = ctx.sourcePos.add(mc.player.getLookAngle()); // 1F
		}

		this.prevOrigin = this.origin;
		this.prevTarget = this.target;
		this.prevFovMod = this.fovMod;
	}

	public boolean tick() {
		prevTotalTick = totalTick;
		prevOrigin = origin;
		prevTarget = target;
		prevFovMod = fovMod;

		for (var step : steps) {
			if (step.resolvedStart == totalTick) {
				if (step.status.isPresent()) {
					mc.gui.setOverlayMessage(step.status.get(), false);
				}

				if (mc.screen != null) {
					if (step.topBar.isPresent()) {
						topBar = mc.font.split(step.topBar.get(), mc.screen.width - 60);
					}

					if (step.bottomBar.isPresent()) {
						bottomBar = mc.font.split(step.bottomBar.get(), mc.screen.width - 60);
					}
				}

				if (step.shader.isPresent()) {
					mc.setPostEffect(step.shader.get());
				}

				if (step.fade.isPresent()) {
					mc.setScreenFade(step.fade.get());
				}

				if (!step.sounds.isEmpty()) {
					for (var sound : step.sounds) {
						var instance = mc.createGlobalSound(sound, variables);
						playingSounds.add(instance);
						mc.getSoundManager().play(instance);
					}
				}
			}

			if (totalTick >= step.resolvedStart && totalTick < step.resolvedStart + step.resolvedLength) {
				float progress = (totalTick - step.resolvedStart) / (float) step.resolvedLength;
				var ctx = new WorldNumberContext(mc.level, progress, variables);
				ctx.originPos = originPos;
				ctx.sourcePos = sourcePos.get();

				step.prevRenderTarget = step.renderTarget;

				if (step.target.isPresent()) {
					var newTarget = step.target.get().get(ctx);

					if (newTarget != null) {
						step.renderTarget = target = newTarget;

						if (step.prevRenderTarget == null) {
							step.prevRenderTarget = target;
						}

						if (step.resolvedStart == totalTick && step.snap.target()) {
							prevTarget = target;
						}
					}
				}

				ctx.targetPos = target;

				if (step.origin.isPresent()) {
					var newOrigin = step.origin.get().get(ctx);

					if (newOrigin != null) {
						origin = newOrigin;

						if (step.resolvedStart == totalTick && step.snap.origin()) {
							prevOrigin = origin;
						}
					}
				}

				if (step.fovModifier.isPresent()) {
					fovMod = step.fovModifier.get().get(ctx);

					if (step.resolvedStart == totalTick && step.snap.zoom()) {
						prevFovMod = fovMod;
					}
				}

				if (step.resolvedStart == totalTick) {
					for (var event : step.events) {
						event.run(mc.level, ctx);
					}
				}
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
		return KMath.lerp(delta, prevFovMod, fovMod);
	}

	@Override
	public boolean renderPlayer() {
		return !cutscene.hidePlayer;
	}

	@Override
	public Vec3 getCameraPosition(float delta) {
		return prevOrigin.lerp(origin, delta);
	}

	@Override
	public Rotation getCameraRotation(float delta, Vec3 cameraPos) {
		return Rotation.compute(cameraPos, prevTarget.lerp(target, delta));
	}

	public void stopped() {
		for (var sound : playingSounds) {
			mc.getSoundManager().stop(sound);
		}

		playingSounds.clear();
		mc.gameRenderer.clearPostEffect();
	}
}
