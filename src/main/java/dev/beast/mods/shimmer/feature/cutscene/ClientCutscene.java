package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.misc.CameraOverride;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.Rotation;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class ClientCutscene implements CameraOverride {
	public static ClientCutscene instance = null;

	public final Minecraft mc;
	public final boolean overrideCamera;
	public final Cutscene cutscene;
	public final WorldNumberVariables variables;
	public final CutsceneStep[] steps;
	public final Supplier<Vec3> sourcePos;
	public int prevTotalTick;
	public int totalTick;
	public int totalLength;

	public Vec3 prevOrigin, origin, prevTarget, target;
	public double prevZoom, zoom;
	public List<FormattedCharSequence> topBar, bottomBar;

	public ClientCutscene(Minecraft mc, boolean overrideCamera, Cutscene cutscene, WorldNumberVariables variables, Supplier<Vec3> sourcePos) {
		this.mc = mc;
		this.overrideCamera = overrideCamera;
		this.cutscene = cutscene;
		this.variables = variables;
		this.steps = cutscene.steps.toArray(new CutsceneStep[0]);
		this.sourcePos = sourcePos;

		this.prevTotalTick = 0;
		this.totalTick = 0;
		this.totalLength = 0;
		this.zoom = 1D;

		var ctx = new WorldNumberContext(mc.level, 0F, variables);
		ctx.sourcePos = sourcePos.get();

		for (var step : steps) {
			this.totalLength = Math.max(totalLength, step.start + step.length);

			if (step.start == 0) {
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

				if (step.zoom.isPresent()) {
					zoom = step.zoom.get().get(ctx);
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
		this.prevZoom = this.zoom;
	}

	public boolean tick() {
		prevTotalTick = totalTick;
		prevOrigin = origin;
		prevTarget = target;
		prevZoom = zoom;

		for (var step : steps) {
			if (step.start == totalTick) {
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
			}

			if (totalTick >= step.start && totalTick < step.start + step.length) {
				float progress = (totalTick - step.start) / (float) step.length;
				var ctx = new WorldNumberContext(mc.level, progress, variables);
				ctx.sourcePos = sourcePos.get();

				step.prevRenderTarget = step.renderTarget;

				if (step.target.isPresent()) {
					var newTarget = step.target.get().get(ctx);

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

				if (step.origin.isPresent()) {
					var newOrigin = step.origin.get().get(ctx);

					if (newOrigin != null) {
						origin = newOrigin;

						if (step.start == totalTick && step.snap.origin()) {
							prevOrigin = origin;
						}
					}
				}

				if (step.zoom.isPresent()) {
					zoom = step.zoom.get().get(ctx);

					if (step.start == totalTick && step.snap.zoom()) {
						prevZoom = zoom;
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
	public double getZoom(double delta) {
		return KMath.lerp(delta, prevZoom, zoom);
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
}
