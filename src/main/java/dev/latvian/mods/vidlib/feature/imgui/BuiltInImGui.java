package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.internal.flag.ImGuiItemFlags;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public class BuiltInImGui {
	public static final List<AdminPanel> OPEN_TABS = new ArrayList<>();

	public static void handle(Minecraft mc) {
		// ImGui.pushFont(ImFonts.getJetbrainsMono19());

		ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 15, 15);
		ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 5F);
		ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 5, 5);
		ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 4F);
		ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 4F);
		ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 4F);
		ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 0);
		ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 12, 8);
		ImGui.pushStyleVar(ImGuiStyleVar.ItemInnerSpacing, 8, 6);
		ImGui.pushStyleVar(ImGuiStyleVar.IndentSpacing, 25F);
		ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarSize, 15F);
		ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarRounding, 9F);
		ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, 5F);
		ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 3F);
		ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0);
		ImGui.pushStyleVar(ImGuiStyleVar.SelectableTextAlign, 0.0F, 0.5F);
		ImGui.pushStyleVar(ImGuiStyleVar.Alpha, 0.95F);

		ImGui.pushStyleColor(ImGuiCol.WindowBg, 0xEF302929);
		ImGui.pushStyleColor(ImGuiCol.TitleBg, 0xEF563847);
		ImGui.pushStyleColor(ImGuiCol.TitleBgActive, 0xEF70517F);

		if ((mc.isLocalServer() || mc.player.hasPermissions(2)) && mc.player.getAdminPanel()) {
			adminPanel(mc);
		}

		NeoForge.EVENT_BUS.post(new ImGuiEvent());

		ImGui.popStyleColor(3);
		ImGui.popStyleVar(17);
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

				if (ImGui.menuItem("Main Stopwatch")) {
					StopwatchPanel.INSTANCE.open();
				}

				if (ImGui.menuItem("New Stopwatch")) {
					new StopwatchPanel().open();
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
