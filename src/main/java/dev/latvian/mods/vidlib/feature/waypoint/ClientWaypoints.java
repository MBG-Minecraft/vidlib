package dev.latvian.mods.vidlib.feature.waypoint;

import dev.latvian.mods.klib.math.DistanceComparator;
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

		for (var waypoint : waypoints) {
			if (waypoint.enabled() && waypoint.dimension() == level.dimension() && waypoint.filter().test(mc.player)) {
				var pos = waypoint.position().get(ctx);

				if (pos != null) {
					list.add(new ScreenWaypoint(waypoint, pos));
				}
			}
		}

		if (list.isEmpty()) {
			return;
		} else if (list.size() >= 2) {
			list.sort(new DistanceComparator<>(mc.gameRenderer.getMainCamera().getPosition(), ScreenWaypoint::pos));
		}

		for (var wp : list) {
			var color = wp.waypoint().tint().withAlpha(wp.waypoint().tint().alphaf() * (WAYPOINT_ALPHA.get() / 255F)).argb();
			var wpos = projectedCoordinates.screen(wp.pos());

			if (wpos != null) {
				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y(), 0F);
				graphics.pose().scale(wpSize / 16F, wpSize / 16F, 1F);

				if (wp.waypoint().centered()) {
					graphics.pose().translate(0F, 8F, 0F);
				}

				if (!Empty.isEmpty(wp.waypoint().label())) {
					var lines = mc.font.split(wp.waypoint().label(), 1000);

					for (int i = 0; i < lines.size(); i++) {
						var line = lines.get(i);
						graphics.drawString(mc.font, line, -mc.font.width(line) / 2F, -18 - (lines.size() * 9) + i * 9, 0xFFFFFFFF, true);
					}
				}

				if (wp.waypoint().showDistance()) {
					var dist = "%,d m".formatted(Mth.floor(mc.gameRenderer.getMainCamera().getPosition().distanceTo(wp.pos())));
					graphics.drawString(mc.font, dist, -mc.font.width(dist) / 2F, 2, 0xFFFFFFFF, true);
				}

				graphics.blit(VidLibRenderTypes.GUI_BLUR, wp.waypoint().icon(), -8, -16, 0F, 0F, 16, 16, 16, 16, color);
				graphics.pose().popPose();
			}
		}
	}
}
