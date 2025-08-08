package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBarRenderer;
import dev.latvian.mods.vidlib.feature.imgui.PropExplorerPanel;
import dev.latvian.mods.vidlib.feature.misc.FlashbackIntegration;
import dev.latvian.mods.vidlib.feature.misc.VLFlashbackIntegration;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
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

	static boolean shouldDrawName(Minecraft mc, Player self, Player player) {
		if (self == player && mc.options.getCameraType().isFirstPerson()) {
			return false;
		}

		return !player.isInvisibleTo(self);
	}

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

		var worldMouse = mc.getWorldMouse();

		if (worldMouse == null) {
			return;
		}

		boolean replay = VLFlashbackIntegration.ENABLED && FlashbackIntegration.isInReplayOrExporting();

		if (replay && !FlashbackIntegration.getRenderNameTags()) {
			return;
		}

		var cam = mc.gameRenderer.getMainCamera().getPosition();
		double minDist = mc.get(InternalServerData.NAME_DRAW_MIN_DIST);
		double midDist = mc.get(InternalServerData.NAME_DRAW_MID_DIST);
		double maxDist = mc.get(InternalServerData.NAME_DRAW_MAX_DIST);
		float minSize = mc.get(InternalServerData.NAME_DRAW_MIN_SIZE);

		var lines = new ArrayList<FormattedCharSequence>(1);
		var delta = deltaTracker.getGameTimeDeltaPartialTick(false);
		var selfDelta = deltaTracker.getGameTimeDeltaPartialTick(true);

		for (var player : level.players()) {
			if (player.isSpectator()) {
				continue;
			}

			if (replay && FlashbackIntegration.isEntityHidden(player.getUUID())) {
				continue;
			}

			if (!shouldDrawName(mc, self, player)) {
				continue;
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

			if (renderName && replay && FlashbackIntegration.isNameHidden(player.getUUID())) {
				renderName = false;
			}

			if (renderHealth && replay && FlashbackIntegration.isHealthHidden(player.getUUID())) {
				renderHealth = false;
			}

			if (renderName || renderHealth) {
				var wpos = worldMouse.screen(pos);

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
					if (PropExplorerPanel.isPropHidden(prop)) {
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

					var wpos = worldMouse.screen(pos);

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

	static void drawFade(GuiGraphics graphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();
		int width = mc.getWindow().getGuiScaledWidth();
		int height = mc.getWindow().getGuiScaledHeight();

		if (mc.player != null) {
			var session = mc.player.vl$sessionData();

			if (session.screenFade != null) {
				session.screenFade.draw(graphics, deltaTracker.getGameTimeDeltaPartialTick(true), width, height);
			}
		}
	}
}
