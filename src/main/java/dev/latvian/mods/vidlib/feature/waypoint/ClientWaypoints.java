package dev.latvian.mods.vidlib.feature.waypoint;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.icon.renderer.IconGuiExtractContext;
import dev.latvian.mods.klib.icon.renderer.IconRenderer;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;

public interface ClientWaypoints {
	ImBoolean VISIBLE = new ImBoolean(true);
	ImBoolean BYPASS_FILTERS = new ImBoolean(false);
	ImFloat SIZE = new ImFloat(36F);
	ImInt ALPHA = new ImInt(255);

	static void fbVisualsMenu(ImGraphics graphics) {
		ImGui.checkbox("Enabled###waypoints-enabled", VISIBLE);
		ImGui.checkbox("Bypass Filters###bypass-filters", BYPASS_FILTERS);

		if (ImGui.smallButton(ImIcons.UNDO + "###reset-size")) {
			SIZE.set(36F);
		}

		ImGui.sameLine();
		ImGui.sliderFloat("Size###size", SIZE.getData(), 0F, 1024F);
		ImGui.sliderInt("Alpha###alpha", ALPHA.getData(), 1, 255);
	}

	static void draw(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();

		if (!VISIBLE.get() || mc.gui.hud.isHidden()) {
			return;
		}

		var level = mc.level;

		if (level == null || mc.player == null) {
			return;
		}

		var waypoints = ClientGameEngine.INSTANCE.getWaypoints(mc);

		if (waypoints.isEmpty()) {
			return;
		}

		var levelSession = ClientLevelSessionData.of(level);

		var projectedCoordinates = levelSession.worldData.projectedCoordinates;

		if (projectedCoordinates == null) {
			return;
		}

		var ctx = levelSession.createGlobalContext();

		int wpSize = (int) (SIZE.get() * mc.getEffectScale());
		var list = new ArrayList<ScreenWaypoint>(waypoints.size());
		var cam = mc.gameRenderer.mainCamera().position();

		for (var waypoint : waypoints) {
			if (BYPASS_FILTERS.get() || waypoint.visible().value().test(mc.player)) {
				var pos = waypoint.position().value().get(ctx);

				if (pos != null) {
					double distance = waypoint.ignoreHeight() ? Vector2d.distance(pos.x, pos.z, cam.x, cam.z) : pos.distanceTo(cam);

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

		graphics.vl$pushShift();

		var wpos = new Vector2f();

		for (var wp : list) {
			float alpha = wp.waypoint().alpha() * (ALPHA.get() / 255F);
			double minDistance = wp.waypoint().minDistance();
			double midDistance = wp.waypoint().midDistance();

			if (minDistance > 0D && midDistance > 0D) {
				alpha *= (float) KMath.map(wp.distance(), minDistance, midDistance, 0D, 1D);
			}

			if (alpha < 0.02F) {
				continue;
			}

			alpha = Math.min(alpha, 1F);

			if (projectedCoordinates.screen(wp.pos(), wpos)) {
				wpos.x = Math.clamp(wpos.x, 16F, projectedCoordinates.width() - 16F);
				wpos.y = Math.clamp(wpos.y, 16F, projectedCoordinates.height() - 16F);

				graphics.pose().translate(wpos.x(), wpos.y());
				graphics.pose().scale(wpSize / 16F, wpSize / 16F);

				if (wp.waypoint().centered()) {
					graphics.pose().translate(0F, 8F);
				}

				var textColor = Color.WHITE.withAlpha(alpha).argb();

				if (!Empty.isEmpty(wp.waypoint().label())) {
					var lines = mc.font.split(wp.waypoint().label(), 1000);

					for (int i = 0; i < lines.size(); i++) {
						var line = lines.get(i);
						graphics.text(mc.font, line, -mc.font.width(line) / 2, -18 - (lines.size() * 9) + i * 9, textColor, true);
					}
				}

				if (wp.waypoint().showDistance()) {
					var dist = "%,d m".formatted(Mth.floor(cam.distanceTo(wp.pos())));
					graphics.text(mc.font, dist, -mc.font.width(dist) / 2, 2, textColor, true);
				}

				IconRenderer.draw(wp.waypoint().icon(), new IconGuiExtractContext(mc, graphics, 0, -8, (int) (alpha * 255)));
			}
		}

		graphics.vl$popShift();
	}
}
