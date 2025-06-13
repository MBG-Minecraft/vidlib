package dev.latvian.mods.vidlib.feature.cutscene;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
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
	public final ClientCutsceneStep[] steps;
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
		this.steps = new ClientCutsceneStep[cutscene.steps.size()];
		this.sourcePos = sourcePos;
		this.originPos = sourcePos.get();

		this.prevTotalTick = 0;
		this.totalTick = 0;
		this.totalLength = 0;
		this.fovMod = 1D;
		this.playingSounds = new ArrayList<>();

		var ctx = mc.level.globalContext(0F).withVariables(variables);
		ctx.originPos = originPos;
		ctx.sourcePos = originPos;

		for (int i = 0; i < cutscene.steps.size(); i++) {
			steps[i] = new ClientCutsceneStep(cutscene.steps.get(i), ctx);
		}

		for (var step : steps) {
			this.totalLength = Math.max(totalLength, step.start + step.length);

			if (step.start == 0) {
				if (step.target != null) {
					var t = step.target.get(ctx);

					if (target == null) {
						target = t;
						step.prevRenderTarget = step.renderTarget = t;
					}
				}

				ctx.targetPos = target;

				if (origin == null && step.origin != null) {
					origin = step.origin.get(ctx);
				}

				if (step.fovModifier != null) {
					fovMod = step.fovModifier.getOr(ctx, 1D);
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
			if (step.start == totalTick) {
				if (step.status != null) {
					mc.gui.setOverlayMessage(step.status, false);
				}

				if (mc.screen != null) {
					if (step.bars != null) {
						if (step.bars.top().isPresent()) {
							topBar = List.copyOf(mc.font.split(step.bars.top().get(), mc.screen.width - 60));
						} else {
							topBar = List.of();
						}

						if (step.bars.bottom().isPresent()) {
							bottomBar = List.copyOf(mc.font.split(step.bars.bottom().get(), mc.screen.width - 60));
						} else {
							bottomBar = List.of();
						}
					}
				}

				if (step.shader != null) {
					mc.setPostEffect(step.shader);
				}

				if (step.fade != null) {
					mc.setScreenFade(step.fade);
				}

				if (!step.sounds.isEmpty()) {
					for (var sound : step.sounds) {
						var instance = mc.createGlobalSound(sound, variables);
						playingSounds.add(instance);
						mc.getSoundManager().play(instance);
					}
				}
			}

			if (totalTick >= step.start && totalTick < step.start + step.length) {
				float progress = (totalTick - step.start) / (float) step.length;
				var ctx = mc.level.globalContext(progress).withVariables(variables);
				ctx.originPos = originPos;
				ctx.sourcePos = sourcePos.get();
				ctx.serverDataMap = mc.getServerData();

				step.prevRenderTarget = step.renderTarget;

				if (step.target != null) {
					var newTarget = step.target.get(ctx);

					if (newTarget != null) {
						step.renderTarget = target = newTarget;

						if (step.prevRenderTarget == null) {
							step.prevRenderTarget = target;
						}

						if (step.start == totalTick && step.snap.target()) {
							prevTarget = target;
						}
					}
				}

				ctx.targetPos = target;

				if (step.origin != null) {
					var newOrigin = step.origin.get(ctx);

					if (newOrigin != null) {
						origin = newOrigin;

						if (step.start == totalTick && step.snap.origin()) {
							prevOrigin = origin;
						}
					}
				}

				if (step.fovModifier != null) {
					fovMod = step.fovModifier.getOr(ctx, 1D);

					if (step.start == totalTick && step.snap.fov()) {
						prevFovMod = fovMod;
					}
				}

				if (step.start == totalTick) {
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
