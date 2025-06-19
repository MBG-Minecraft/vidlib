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
			if (ImGui.beginMenu("Open...")) {
				if (ImGui.menuItem("Memory Usage")) {
					MemoryUsagePanel.INSTANCE.open();
				}

				if (ImGui.menuItem("Command History")) {
					CommandHistoryPanel.INSTANCE.open();
				}

				if (ImGui.menuItem("Stopwatch")) {
					StopwatchPanel.INSTANCE.open();
				}

				if (ImGui.menuItem("New Stopwatch")) {
					new StopwatchPanel("stopwatch-" + UUID.randomUUID().toString().toLowerCase(Locale.ROOT), true).open();
				}

				NeoForge.EVENT_BUS.post(new AdminPanelEvent.OpenDropdown());
				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Debug")) {
				imgui.internal.ImGui.pushItemFlag(ImGuiItemFlags.SelectableDontClosePopup, true);

				if (ImGui.menuItem("Entity Hitboxes", null, mc.getEntityRenderDispatcher().shouldRenderHitBoxes())) {
					mc.getEntityRenderDispatcher().setRenderHitBoxes(!mc.getEntityRenderDispatcher().shouldRenderHitBoxes());
				}

				if (ImGui.menuItem("Chunk Borders", null, mc.debugRenderer.renderChunkborder)) {
					mc.debugRenderer.switchRenderChunkborder();
				}

				if (ImGui.menuItem("Capture Frustum", null, mc.levelRenderer.getCapturedFrustum() != null)) {
					if (mc.levelRenderer.getCapturedFrustum() != null) {
						mc.levelRenderer.killFrustum();
					} else {
						mc.levelRenderer.captureFrustum();
					}
				}

				if (ImGui.menuItem("Zones", null, mc.player.getShowZones())) {
					mc.runClientCommand("zones show");
				}

				if (ImGui.menuItem("Anchor", null, mc.player.getShowAnchor())) {
					mc.runClientCommand("anchor show");
				}

				if (ImGui.menuItem("Widgets")) {
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
