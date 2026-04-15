package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBarRenderer;
import dev.latvian.mods.vidlib.feature.font.MSDFFont;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.waypoint.ClientWaypoints;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.function.Predicate;

public interface VidLibHUD {
	Mutable<Predicate<Player>> DEFAULT_DRAW_NAME = new MutableObject<>(player -> !player.isBoss());
	Mutable<Predicate<Player>> DEFAULT_DRAW_HEALTH_BAR = new MutableObject<>(player -> player.isSurvivalLike() && !player.isBoss());

	static void drawPlayerNames(GuiGraphics graphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();
		var level = mc.level;
		var self = mc.player;

		if (level == null || self == null || mc.options.hideGui || mc.screen != null && mc.screen.hidePlayerNames()) {
			return;
		}

		var nameDrawType = mc.getNameDrawType();

		if (nameDrawType == NameDrawType.VANILLA) {
			return;
		}

		var projectedCoordinates = mc.getProjectedCoordinates();

		if (projectedCoordinates == null) {
			return;
		}

		boolean replay = ReplayAPI.getActive().isInReplayOrExporting();

		if (replay && !ReplayAPI.getActive().getRenderNameTags()) {
			return;
		}

		var cam = mc.gameRenderer.getMainCamera().getPosition();
		double minDist = mc.get(InternalServerData.NAME_DRAW_MIN_DIST);
		double midDist = mc.get(InternalServerData.NAME_DRAW_MID_DIST);
		double maxDist = mc.get(InternalServerData.NAME_DRAW_MAX_DIST);
		float minSize = mc.get(InternalServerData.NAME_DRAW_MIN_SIZE);
		boolean mustSee = mc.get(InternalServerData.NAME_MUST_SEE);

		var lines = new ArrayList<FormattedCharSequence>(1);
		var delta = deltaTracker.getGameTimeDeltaPartialTick(false);
		var selfDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
		var local = mc.player;

		for (var player : level.players()) {
			if (replay && ReplayAPI.getActive().isEntityHidden(player.getUUID())) {
				continue;
			}

			if (!ClientGameEngine.INSTANCE.shouldRender2DPlayerName(mc, mc.player, player)) {
				continue;
			}

			if (mustSee && local != null && player != local && mc.level != null) {
				Vec3 start = local.getEyePosition(selfDelta);
				Vec3 end = player.getEyePosition(player == self ? selfDelta : delta);
				ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, local);
				HitResult result = mc.level.clip(context);
				if (result.getType() == HitResult.Type.BLOCK) {
					continue;
				}
			}

			var pos = player.getPosition(player == self ? selfDelta : delta).add(0D, player.getBbHeight() * 1.1D, 0D);
			var distSq = cam.distanceToSqr(pos);

			if (distSq > maxDist * maxDist) {
				continue;
			}

			var dist = Math.sqrt(distSq);

			var alpha = (int) Math.clamp(KMath.map(dist, midDist, maxDist, 255F, 0F), 0F, 255F);

			if (alpha <= 3) {
				continue;
			}

			var renderName = nameDrawType.renderName.resolve(DEFAULT_DRAW_NAME.getValue().test(player));
			var renderHealth = nameDrawType.renderHealth.resolve(DEFAULT_DRAW_HEALTH_BAR.getValue().test(player));

			if (renderName && replay && ReplayAPI.getActive().isNameHidden(player.getUUID())) {
				renderName = false;
			}

			if (renderHealth && replay && ReplayAPI.getActive().isHealthHidden(player.getUUID())) {
				renderHealth = false;
			}

			if (renderName || renderHealth) {
				var wpos = projectedCoordinates.screen(pos);

				if (wpos != null) {
					var scale = (float) Math.clamp(KMath.map(dist, minDist, midDist, 1F, minSize), minSize, 1F);

					lines.clear();

					if (renderName) {
						lines.addAll(mc.font.split(player.getDisplayName(), 1000));

						var scoreboard = player.getScoreboard();
						var objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);

						if (objective != null) {
							ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(player, objective);
							Component component = ReadOnlyScoreInfo.safeFormatValue(readonlyscoreinfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
							lines.addAll(mc.font.split(Component.empty().append(component).append(CommonComponents.SPACE).append(objective.getDisplayName()), 1000));
						}

						NeoForge.EVENT_BUS.post(new HUDNameEvent(player, mc.font, lines));
					}

					float health = renderHealth ? player.getRelativeHealth(player == self ? selfDelta : delta) : -1F;

					graphics.pose().pushPose();
					graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
					graphics.pose().scale(scale, scale, 1F);
					graphics.healthBarWithText(mc.font, -20, -3, 40, 6, lines, health, alpha);
					graphics.pose().popPose();
				}
			}
		}

		for (var propList : level.getProps().propLists.values()) {
			for (var prop : propList) {
				boolean renderName = prop.shouldRenderDisplayName(self);
				boolean renderHealth = prop.shouldRenderHealth(self);

				if (renderName || renderHealth) {
					if (ClientProps.isPropHidden(prop)) {
						continue;
					}

					var pos = prop.getInfoPos(delta);
					var distSq = cam.distanceToSqr(pos);

					if (distSq > maxDist * maxDist) {
						continue;
					}

					var dist = Math.sqrt(distSq);

					var alpha = (int) Math.clamp(KMath.map(dist, midDist, maxDist, 255F, 0F), 0F, 255F);

					if (alpha <= 3) {
						continue;
					}

					var wpos = projectedCoordinates.screen(pos);

					if (wpos != null) {
						var scale = (float) Math.clamp(KMath.map(dist, minDist, midDist, 1F, minSize), minSize, 1F);

						lines.clear();

						if (renderName) {
							lines.addAll(mc.font.split(prop.getDisplayName(), 1000));
						}

						float health = renderHealth ? prop.getDisplayHealth(delta) : -1F;

						graphics.pose().pushPose();
						graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
						graphics.pose().scale(scale, scale, 1F);
						graphics.healthBarWithText(mc.font, -20, -3, 40, 6, lines, health, alpha);
						graphics.pose().popPose();
					}
				}
			}
		}
	}

	static void drawAboveBossOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();
		ProgressBarRenderer.draw(mc, graphics, deltaTracker);
		CanvasImpl.drawPreview(mc, graphics);
	}

	static void drawBelowAll(GuiGraphics graphics, DeltaTracker deltaTracker) {
		ClientWaypoints.draw(graphics, deltaTracker);
		drawPlayerNames(graphics, deltaTracker);
		MSDFFont.drawDebugText(graphics, deltaTracker);
	}

	static void drawAboveAll(GuiGraphics graphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();

		if (mc.player != null) {
			var text = ClientGameEngine.INSTANCE.blockedScreenText(mc.player);

			if (text != null) {
				graphics.fill(0, 0, width, height, 990, 0xFF000000);
				graphics.pose().pushPose();
				graphics.pose().translate(width / 2F, height / 3F, 1000F);

				var lines = mc.font.split(text, width * 4 / 5);

				for (int i = 0; i < lines.size(); i++) {
					graphics.drawString(mc.font, lines.get(i), -mc.font.width(lines.get(i)) / 2, -4 + i * 10, 0xFFFFFFFF);
				}

				graphics.pose().popPose();
			} else {
				Pins.draw(graphics, deltaTracker);

				var session = mc.player.vl$sessionData();

				if (session.screenFade != null) {
					session.screenFade.draw(graphics, deltaTracker.getGameTimeDeltaPartialTick(true), width, height);
				}
			}
		}
	}

	static void drawInformationHUD(GuiGraphics graphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();

		if (mc.player == null) {
			return;
		}

		var info = ClientGameEngine.INSTANCE.getInformationHUD(mc, mc.player, deltaTracker);

		if (info.isEmpty()) {
			return;
		}

		Profiler.get().push("vidlib");
		Profiler.get().push("information_hud");
		var font = mc.gui.getFont();

		int maxWidth = 0;

		for (var line : info) {
			maxWidth = Math.max(maxWidth, font.width(line));
		}

		int xoff = (graphics.guiWidth() - maxWidth) / 2;
		int yoff = 8;

		TooltipRenderUtil.renderTooltipBackground(graphics, xoff, yoff, maxWidth, info.size() * 10 - 2, 0, null);

		for (int l = 0; l < info.size(); l++) {
			var line = info.get(l);
			boolean center = false;

			if (line.getStyle().isObfuscated()) {
				line = line.copy().setStyle(line.getStyle().withObfuscated(false));
				center = true;
			}

			graphics.drawString(font, line, xoff + (center ? (maxWidth - font.width(line)) / 2 : 0), yoff + l * 10, 0xFFFFFFFF);
		}

		Profiler.get().pop();
		Profiler.get().pop();
	}
}
