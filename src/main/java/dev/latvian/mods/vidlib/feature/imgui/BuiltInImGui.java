package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.internal.flag.ImGuiItemFlags;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BuiltInImGui {
	public static final List<AdminPanel> OPEN_TABS = new ArrayList<>();

	public static void handle(Minecraft mc) {
		ImGuiUtils.pushDefaultStyle();

		if ((mc.isLocalServer() || mc.player.hasPermissions(2)) && mc.player.getAdminPanel()) {
			adminPanel(mc);
		}

		NeoForge.EVENT_BUS.post(new ImGuiEvent());
		ImGuiUtils.popDefaultStyle();
	}

	public static void adminPanel(Minecraft mc) {
		if (ImGui.beginMainMenuBar()) {
			if (ImGui.beginMenu(ImIcons.OPEN + " Open")) {
				if (ImGui.menuItem(ImIcons.MEMORY + " Memory Usage")) {
					MemoryUsagePanel.INSTANCE.open();
				}

				if (ImGui.menuItem(ImIcons.CODE + " Command History")) {
					CommandHistoryPanel.INSTANCE.open();
				}

				if (ImGui.menuItem(ImIcons.TIMELAPSE + " Stopwatch")) {
					StopwatchPanel.INSTANCE.open();
				}

				if (ImGui.menuItem(ImIcons.TIMELAPSE + " New Stopwatch")) {
					new StopwatchPanel("stopwatch-" + UUID.randomUUID().toString().toLowerCase(Locale.ROOT), true).open();
				}

				NeoForge.EVENT_BUS.post(new AdminPanelEvent.OpenDropdown());
				ImGui.endMenu();
			}

			if (ImGui.beginMenu(ImIcons.BUG + " Debug")) {
				imgui.internal.ImGui.pushItemFlag(ImGuiItemFlags.SelectableDontClosePopup, true);

				if (ImGui.menuItem(ImIcons.SELECT + " Entity Hitboxes", null, mc.getEntityRenderDispatcher().shouldRenderHitBoxes())) {
					mc.getEntityRenderDispatcher().setRenderHitBoxes(!mc.getEntityRenderDispatcher().shouldRenderHitBoxes());
				}

				if (ImGui.menuItem(ImIcons.SELECT + " Chunk Borders", null, mc.debugRenderer.renderChunkborder)) {
					mc.debugRenderer.switchRenderChunkborder();
				}

				if (ImGui.menuItem(ImIcons.APERTURE + " Capture Frustum", null, mc.levelRenderer.getCapturedFrustum() != null)) {
					if (mc.levelRenderer.getCapturedFrustum() != null) {
						mc.levelRenderer.killFrustum();
					} else {
						mc.levelRenderer.captureFrustum();
					}
				}

				if (ImGui.menuItem(ImIcons.SELECT + " Zones", null, mc.player.getShowZones())) {
					mc.runClientCommand("zones show");
				}

				if (ImGui.menuItem(ImIcons.SELECT + " Anchor", null, mc.player.getShowAnchor())) {
					mc.runClientCommand("anchor show");
				}

				if (ImGui.menuItem(ImIcons.SELECT + " Widgets")) {
					new WidgetDebugPanel().open();
				}

				NeoForge.EVENT_BUS.post(new AdminPanelEvent.DebugDropdown());
				imgui.internal.ImGui.popItemFlag();
				ImGui.endMenu();
			}

			NeoForge.EVENT_BUS.post(new AdminPanelEvent.MenuBar());
			ImGui.endMainMenuBar();
		}

		OPEN_TABS.removeIf(AdminPanel::handle);
	}
}
