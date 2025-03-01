package dev.beast.mods.shimmer.feature.cutscene;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ClientCutscene {
	public static ClientCutscene instance = null;

	public final Minecraft mc;
	public final Cutscene cutscene;
	public final CutsceneStep[] steps;
	public int prevTotalTick;
	public int totalTick;
	public int totalLength;

	public Vec3 prevOrigin, origin, prevTarget, target;
	public double prevZoom, zoom;
	public List<FormattedCharSequence> topBar, bottomBar;

	public ClientCutscene(Minecraft mc, Cutscene cutscene) {
		this.mc = mc;
		this.cutscene = cutscene;
		this.steps = cutscene.steps.toArray(new CutsceneStep[0]);
		this.prevTotalTick = 0;
		this.totalTick = 0;
		this.totalLength = 0;
		this.zoom = 1D;

		for (var step : steps) {
			this.totalLength = Math.max(totalLength, step.start + step.length);

			if (step.start == 0) {
				if (origin == null && step.origin != null) {
					origin = step.origin.get(mc.level, 0F);
				}

				if (step.target != null) {
					var t = step.target.get(mc.level, 0F);

					if (target == null) {
						target = t;
						step.prevRenderTarget = step.renderTarget = t;
					}
				}

				if (step.zoom != null) {
					zoom = step.zoom.get(mc.level, 0F);
				}
			}
		}

		if (origin == null || target == null) {
			var eye = mc.player.getEyePosition();

			if (origin == null) {
				origin = eye;
			}

			if (target == null) {
				target = eye.add(mc.player.getLookAngle()); // 1F
			}
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

		float totalProgress = totalTick / (float) totalLength;

		if (cutscene.tick != null) {
			for (var tick : cutscene.tick) {
				tick.tick(mc.level, totalProgress);
			}
		}

		for (var step : steps) {
			if (step.start == totalTick) {
				if ((step.flags & CutsceneStep.STATUS) != 0) {
					mc.gui.setOverlayMessage(step.status == null ? Component.empty() : step.status, false);
				}

				if ((step.flags & CutsceneStep.TOP_BAR) != 0 && mc.screen != null) {
					topBar = step.topBar == null ? null : mc.font.split(step.topBar, mc.screen.width - 60);
				}

				if ((step.flags & CutsceneStep.BOTTOM_BAR) != 0 && mc.screen != null) {
					bottomBar = step.bottomBar == null ? null : mc.font.split(step.bottomBar, mc.screen.width - 60);
				}

				if ((step.flags & CutsceneStep.SHADER) != 0) {
					// FIXME: MonochromeClient.loadPostProcessor(mc, step.shader);
				}
			}

			if (totalTick >= step.start && totalTick < step.start + step.length) {
				float progress = (totalTick - step.start) / (float) step.length;

				if (step.tick != null) {
					for (var tick : step.tick) {
						tick.tick(mc.level, progress);
					}
				}

				if (step.origin != null) {
					origin = step.origin.get(mc.level, progress);

					if (step.start == totalTick && (step.flags & CutsceneStep.SNAP_ORIGIN) != 0) {
						prevOrigin = origin;
					}
				}

				step.prevRenderTarget = step.renderTarget;

				if (step.target != null) {
					target = step.target.get(mc.level, progress);
					step.renderTarget = target;

					if (step.prevRenderTarget == null) {
						step.prevRenderTarget = target;
					}

					if (step.start == totalTick && (step.flags & CutsceneStep.SNAP_TARGET) != 0) {
						prevTarget = target;
					}
				}

				if (step.zoom != null) {
					zoom = step.zoom.get(mc.level, progress);

					if (step.start == totalTick && (step.flags & CutsceneStep.SNAP_ZOOM) != 0) {
						prevZoom = zoom;
					}
				}
			}
		}

		totalTick++;

		// mc.setScreen(previousScreen);
		return totalTick >= totalLength;
	}
}
