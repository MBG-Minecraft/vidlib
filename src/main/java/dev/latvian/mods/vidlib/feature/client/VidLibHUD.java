package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.canvas.CanvasImpl;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.entity.progress.ProgressBarRenderer;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

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

		var cam = mc.gameRenderer.getMainCamera().getPosition();
		double minDist = mc.get(InternalServerData.NAME_DRAW_MIN_DIST);
		double midDist = mc.get(InternalServerData.NAME_DRAW_MID_DIST);
		double maxDist = mc.get(InternalServerData.NAME_DRAW_MAX_DIST);
		float minSize = mc.get(InternalServerData.NAME_DRAW_MIN_SIZE);

		for (var player : level.players()) {
			if (!shouldDrawName(mc, self, player)) {
				continue;
			}

			if (player.isSpectator()) {
				continue;
			}

			var pos = player.getPosition(deltaTracker.getGameTimeDeltaPartialTick(player == self)).add(0D, player.getBbHeight() * 1.1D, 0D);
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

				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
				graphics.pose().scale(scale, scale, 1F);
				var renderName = nameDrawType.renderName.resolve(DEFAULT_DRAW_NAME.getValue().test(player));
				var renderHealth = nameDrawType.renderHealth.resolve(DEFAULT_DRAW_HEALTH_BAR.getValue().test(player));
				graphics.healthBarWithLabel(mc.font, player, -20, -3, 40, 6, renderName, renderHealth, alpha);
				graphics.pose().popPose();
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
