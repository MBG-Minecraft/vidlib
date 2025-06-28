package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.canvas.CanvasPanel;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneBuilderPanel;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.skybox.Skyboxes;
import dev.latvian.mods.vidlib.feature.sound.SoundEventImBuilder;
import imgui.ImGui;
import imgui.internal.flag.ImGuiItemFlags;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BuiltInImGui {
	public static final List<AdminPanel> OPEN_TABS = new ArrayList<>();
	public static final ImBoolean SHOW_STACK_TOOL = new ImBoolean(false);
	public static final ImBoolean SHOW_STYLE_EDITOR_TOOL = new ImBoolean(false);
	public static Boolean showSounds = null;

	public static void handle(Minecraft mc) {
		var graphics = new ImGraphics();
		graphics.pushStack();
		graphics.setDefaultStyle();
		graphics.setNumberType(ImNumberType.DOUBLE);
		graphics.setNumberRange(null);

		if ((mc.isLocalServer() || mc.player.hasPermissions(2)) && mc.player.getAdminPanel()) {
			ImGuiHooks.enable();
			adminPanel(mc, graphics);
		}

		NeoForge.EVENT_BUS.post(new ImGuiEvent(graphics));

		if (SHOW_STACK_TOOL.get()) {
			ImGui.showStackToolWindow();
		}

		if (SHOW_STYLE_EDITOR_TOOL.get()) {
			ImGui.showStyleEditor();
		}

		if (showSounds != null) {
			if (!showSounds) {
				ImGui.openPopup("###sound-modal");
				showSounds = true;
			}

			if (SoundEventImBuilder.soundModal(null).isAny() || !SoundEventImBuilder.PREVIEW_OPEN.get()) {
				showSounds = null;
			}
		}

		graphics.popStack();
	}

	public static void adminPanel(Minecraft mc, ImGraphics graphics) {
		var session = mc.player.vl$sessionData();

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

				if (ImGui.beginMenu(ImIcons.APERTURE + " Canvas")) {
					CanvasPanel.menu();
					ImGui.endMenu();
				}

				if (ImGui.menuItem(ImIcons.CAMERA + " Cutscene Builder")) {
					CutsceneBuilderPanel.INSTANCE.open();
				}

				NeoForge.EVENT_BUS.post(new AdminPanelEvent.OpenDropdown(graphics));

				if (ImGui.menuItem(ImIcons.FRAMED_CUBE + " Debug Widgets")) {
					WidgetDebugPanel.INSTANCE.open();
				}

				ImGui.menuItem(ImIcons.MEMORY + " ID Stack Tool", null, SHOW_STACK_TOOL);
				ImGui.menuItem(ImIcons.EDIT + " Style Editor Tool", null, SHOW_STYLE_EDITOR_TOOL);

				if (ImGui.menuItem(ImIcons.PLAY + " Sounds")) {
					showSounds = false;
				}

				ImGui.endMenu();
			}

			if (ImGui.beginMenu(ImIcons.SETTINGS + " Config")) {
				if (ImGui.beginMenu(ImIcons.BRIGHTNESS + " Skybox")) {
					graphics.pushStack();
					graphics.setItemFlag(ImGuiItemFlags.SelectableDontClosePopup, true);
					var current = mc.level.getSkybox();

					for (var skybox : SkyboxData.SKYBOX_IDS) {
						var tex = session.getSkybox(skybox).loadTexture(mc);
						ImGui.image(tex.getTexture().vl$getHandle(), 18F, 18F, 0F, 0.5F, 0.25F, 1F);
						ImGui.sameLine();

						if (ImGui.menuItem(skybox.getPath(), null, skybox.equals(current))) {
							mc.runClientCommand("skybox set \"" + skybox + "\"");
						}
					}

					if (ImGui.menuItem("Vanilla", null, Skyboxes.VANILLA.equals(current))) {
						mc.runClientCommand("skybox set \"minecraft:vanilla\"");
					}

					graphics.popStack();
					ImGui.endMenu();
				}

				NeoForge.EVENT_BUS.post(new AdminPanelEvent.ConfigDropdown(graphics));
				ImGui.endMenu();
			}

			if (ImGui.beginMenu(ImIcons.BUG + " Debug")) {
				if (ImGui.menuItem(ImIcons.RELOAD + " Reload Assets")) {
					mc.reloadResourcePacks();
				}

				if (ImGui.menuItem(ImIcons.RELOAD + " Reload Data")) {
					mc.runClientCommand("reload");
				}

				if (ImGui.menuItem(ImIcons.APERTURE + " Capture Frustum", null, mc.levelRenderer.getCapturedFrustum() != null)) {
					if (mc.levelRenderer.getCapturedFrustum() != null) {
						mc.levelRenderer.killFrustum();
					} else {
						mc.levelRenderer.captureFrustum();
					}
				}

				if (ImGui.menuItem(ImIcons.STOP + " Stop all Sounds")) {
					Minecraft.getInstance().getSoundManager().stop();
				}

				NeoForge.EVENT_BUS.post(new AdminPanelEvent.DebugDropdown(graphics));
				ImGui.endMenu();
			}

			if (ImGui.beginMenu(ImIcons.VISIBLE + " Show")) {
				graphics.pushStack();
				graphics.setItemFlag(ImGuiItemFlags.SelectableDontClosePopup, true);

				if (ImGui.menuItem(ImIcons.SELECT + " Entity Hitboxes", null, mc.getEntityRenderDispatcher().shouldRenderHitBoxes())) {
					mc.getEntityRenderDispatcher().setRenderHitBoxes(!mc.getEntityRenderDispatcher().shouldRenderHitBoxes());
				}

				if (ImGui.menuItem(ImIcons.SELECT + " Chunk Borders", null, mc.debugRenderer.renderChunkborder)) {
					mc.debugRenderer.switchRenderChunkborder();
				}

				if (ImGui.menuItem(ImIcons.FULLSCREEN + " Zones", null, mc.player.getShowZones())) {
					mc.runClientCommand("zones show");
				}

				if (ImGui.menuItem(ImIcons.FULLSCREEN + " Anchor", null, mc.player.getShowAnchor())) {
					mc.runClientCommand("anchor show");
				}

				NeoForge.EVENT_BUS.post(new AdminPanelEvent.ShowDropdown(graphics));
				graphics.popStack();
				ImGui.endMenu();
			}

			NeoForge.EVENT_BUS.post(new AdminPanelEvent.MenuBar(graphics));
			ImGui.endMainMenuBar();
		}

		OPEN_TABS.removeIf(panel -> panel.handle(graphics));
	}
}
