package dev.latvian.mods.vidlib.feature.waypoint;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Comparator;

public interface ClientWaypoints {
	ImBoolean ENABLED = new ImBoolean(true);
	ImFloat WAYPOINT_SIZE = new ImFloat(32F);
	ImInt WAYPOINT_ALPHA = new ImInt(255);

	static void draw(GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (!ENABLED.get()) {
			return;
		}

		var mc = Minecraft.getInstance();
		var level = mc.level;

		if (level == null || mc.player == null) {
			return;
		}

		var waypoints = ClientGameEngine.INSTANCE.getWaypoints(mc);

		if (waypoints.isEmpty()) {
			return;
		}

		var projectedCoordinates = mc.getProjectedCoordinates();

		if (projectedCoordinates == null) {
			return;
		}

		var ctx = level.getGlobalContext();
		WAYPOINT_SIZE.set(36F);

		int wpSize = (int) (WAYPOINT_SIZE.get() * mc.getEffectScale());
		var list = new ArrayList<ScreenWaypoint>(waypoints.size());
		var cam = mc.gameRenderer.getMainCamera().getPosition();

		for (var waypoint : waypoints) {
			if (waypoint.dimension() == level.dimension() && waypoint.visible().test(mc.player)) {
				var pos = waypoint.position().get(ctx);

				if (pos != null) {
					double distance = pos.distanceTo(cam);

					if (waypoint.maxDistance() <= 0D || distance <= waypoint.maxDistance()) {
						if (waypoint.minDistance() <= 0D || distance >= waypoint.minDistance()) {
							list.add(new ScreenWaypoint(waypoint, pos, distance));
						}
					}
				}
			}
		}

		if (list.isEmpty()) {
			return;
		} else if (list.size() >= 2) {
			list.sort(Comparator.comparingDouble(ScreenWaypoint::distance).reversed());
		}

		for (var wp : list) {
			float alpha = wp.waypoint().tint().alphaf() * (WAYPOINT_ALPHA.get() / 255F);
			double minDistance = wp.waypoint().minDistance();
			double midDistance = wp.waypoint().midDistance();

			if (minDistance > 0D && midDistance > 0D) {
				alpha *= (float) KMath.map(wp.distance(), minDistance, midDistance, 0D, 1D);
			}

			if (alpha < 0.02F) {
				continue;
			}

			alpha = Math.min(alpha, 1F);

			var wpos = projectedCoordinates.screen(wp.pos());

			if (wpos != null) {
				wpos.x = Math.clamp(wpos.x, 16F, projectedCoordinates.width() - 16F);
				wpos.y = Math.clamp(wpos.y, 16F, projectedCoordinates.height() - 16F);

				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y(), 0F);
				graphics.pose().scale(wpSize / 16F, wpSize / 16F, 1F);

				if (wp.waypoint().centered()) {
					graphics.pose().translate(0F, 8F, 0F);
				}

				var textColor = Color.WHITE.withAlpha(alpha).argb();

				if (!Empty.isEmpty(wp.waypoint().label())) {
					var lines = mc.font.split(wp.waypoint().label(), 1000);

					for (int i = 0; i < lines.size(); i++) {
						var line = lines.get(i);
						graphics.drawString(mc.font, line, -mc.font.width(line) / 2F, -18 - (lines.size() * 9) + i * 9, textColor, true);
					}
				}

				if (wp.waypoint().showDistance()) {
					var dist = "%,d m".formatted(Mth.floor(cam.distanceTo(wp.pos())));
					graphics.drawString(mc.font, dist, -mc.font.width(dist) / 2F, 2, textColor, true);
				}

				graphics.blit(VidLibRenderTypes.GUI_BLUR, wp.waypoint().icon(), -8, -16, 0F, 0F, 16, 16, 16, 16, wp.waypoint().tint().withAlpha(alpha).argb());
				graphics.pose().popPose();
			}
		}
	}
}
