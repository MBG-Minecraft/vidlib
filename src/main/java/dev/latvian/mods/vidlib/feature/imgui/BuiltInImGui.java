package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.canvas.CanvasPanel;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneBuilderPanel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.skybox.Skybox;
import dev.latvian.mods.vidlib.feature.sound.SoundEventImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public class BuiltInImGui {
	public static final List<AdminPanel> OPEN_PANELS = new ArrayList<>();
	public static boolean mainMenuOpen = true;
	public static final ImBoolean SHOW_STACK_TOOL = new ImBoolean(false);
	public static final ImBoolean SHOW_STYLE_EDITOR_TOOL = new ImBoolean(false);
	public static Boolean showSounds = null;

	public static final MenuItem OPEN = MenuItem.menu(ImIcons.OPEN, "Open", (graphics, list) -> {
		list.add(MenuItem.item(ImIcons.MEMORY, "Memory Usage", MemoryUsagePanel.INSTANCE));
		list.add(MenuItem.item(ImIcons.CODE, "Command History", CommandHistoryPanel.INSTANCE));
		list.add(MenuItem.item(ImIcons.TIMELAPSE, "New Stopwatch", g -> StopwatchPanel.openNew()));
		list.add(MenuItem.menu(ImIcons.APERTURE, "Canvas", CanvasPanel::menu));
		list.add(MenuItem.item(ImIcons.PLAY, "Sounds", BuiltInImGui.showSounds != null, g -> BuiltInImGui.showSounds = false));
		list.add(MenuItem.SEPARATOR);
		list.add(MenuItem.item(ImIcons.CAMERA, "Cutscene Builder", CutsceneBuilderPanel.INSTANCE).enabled(graphics.inGame));
		list.add(MenuItem.item(ImIcons.SEARCH, "Entity Explorer", EntityExplorerPanel.INSTANCE).enabled(graphics.inGame));
		list.add(MenuItem.item(ImIcons.SEARCH, "Prop Explorer", PropExplorerPanel.INSTANCE).enabled(graphics.inGame));

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.OpenDropdown(graphics, list));

		list.add(MenuItem.SEPARATOR);
		list.add(MenuItem.item(ImIcons.FRAMED_CUBE, "Debug Widgets", WidgetDebugPanel.INSTANCE));
		list.add(MenuItem.item(ImIcons.MEMORY, "ID Stack Tool", SHOW_STACK_TOOL));
		list.add(MenuItem.item(ImIcons.EDIT, "Style Editor Tool", SHOW_STYLE_EDITOR_TOOL));
	});

	public static final MenuItem CONFIG = MenuItem.menu(ImIcons.SETTINGS, "Config", (graphics, list) -> {
		if (graphics.inGame) {
			list.add(Skybox.MENU_ITEM);
		}

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.ConfigDropdown(graphics, list));
	});

	public static final MenuItem DEBUG = MenuItem.menu(ImIcons.BUG, "Debug", (graphics, list) -> {
		list.add(MenuItem.item(ImIcons.RELOAD, "Reload Assets", g -> g.mc.reloadResourcePacks()));
		list.add(MenuItem.item(ImIcons.RELOAD, "Reload Data", g -> g.mc.runClientCommand("reload")).enabled(graphics.isAdmin));

		list.add(MenuItem.SEPARATOR);

		list.add(MenuItem.item(ImIcons.APERTURE, "Capture Frustum", graphics.mc.levelRenderer.getCapturedFrustum() != null, g -> {
			if (g.mc.levelRenderer.getCapturedFrustum() != null) {
				g.mc.levelRenderer.killFrustum();
			} else {
				g.mc.levelRenderer.captureFrustum();
			}
		}).enabled(graphics.inGame));

		list.add(MenuItem.item(ImIcons.STOP, "Stop all Sounds", g -> g.mc.getSoundManager().stop()));

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.DebugDropdown(graphics, list));
	});

	public static final MenuItem SHOW = MenuItem.menu(ImIcons.VISIBLE, "Show", (graphics, list) -> {
		list.add(MenuItem.item(ImIcons.SELECT, "Entity Hitboxes", graphics.mc.getEntityRenderDispatcher().shouldRenderHitBoxes(), g -> g.mc.getEntityRenderDispatcher().setRenderHitBoxes(!g.mc.getEntityRenderDispatcher().shouldRenderHitBoxes())).enabled(graphics.inGame));

		list.add(MenuItem.item(ImIcons.SELECT, "Chunk Borders", graphics.mc.debugRenderer.renderChunkborder, g -> g.mc.debugRenderer.switchRenderChunkborder()).enabled(graphics.inGame));
		list.add(MenuItem.item(ImIcons.SELECT, "Octree", graphics.mc.debugRenderer.renderOctree, g -> g.mc.debugRenderer.toggleRenderOctree()).enabled(graphics.inGame));
		list.add(MenuItem.item(ImIcons.SELECT, "Section Path", graphics.mc.sectionPath, g -> g.mc.sectionPath = !g.mc.sectionPath).enabled(graphics.inGame));
		list.add(MenuItem.item(ImIcons.SELECT, "Section Visibility", graphics.mc.sectionVisibility, g -> g.mc.sectionVisibility = !g.mc.sectionVisibility).enabled(graphics.inGame));

		list.add(MenuItem.SEPARATOR);

		list.add(MenuItem.item(ImIcons.FULLSCREEN, "Zones", VidLibClientOptions.getShowZones(), g -> {
			VidLibClientOptions.SHOW_ZONES.set(!VidLibClientOptions.getShowZones());
			g.mc.options.save();
		}).enabled(graphics.isAdmin));

		if (VidLibClientOptions.getShowZones()) {
			list.add(MenuItem.item(ImIcons.FULLSCREEN, "Zone Outer Bounds", VidLibClientOptions.getShowZoneOuterBounds(), g -> {
				VidLibClientOptions.SHOW_ZONE_OUTER_BOUNDS.set(!VidLibClientOptions.getShowZoneOuterBounds());
				g.mc.options.save();
			}).enabled(graphics.isAdmin));
		}

		list.add(MenuItem.item(ImIcons.ANCHOR, "Anchor", VidLibClientOptions.getShowAnchor(), g -> {
			VidLibClientOptions.SHOW_ANCHOR.set(!VidLibClientOptions.getShowAnchor());
			g.mc.options.save();
		}).enabled(graphics.isAdmin));

		list.add(MenuItem.SEPARATOR);

		list.add(MenuItem.item(ImIcons.TIMELAPSE, "Stopwatch", GlobalStopwatchPanel.INSTANCE).enabled(graphics.inGame));

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.ShowDropdown(graphics, list));
	}).remainOpen(true);

	public static final MenuItem WARP = MenuItem.menu(ImIcons.LOCATION, "Warp", (graphics, list) -> {
		if (graphics.inGame) {
			list.add(MenuItem.menu(ImIcons.WORLD, "Dimension", (g1, list1) -> {
				var registry = g1.mc.player.connection.levels();

				for (var dimension : registry) {
					list1.add(MenuItem.item(dimension.location().toString(), g -> g.mc.runClientCommand("execute in " + dimension.location() + " run tp @s ~ ~ ~")).disabled(graphics.isReplay));
				}
			}).enabled(graphics.isAdmin));
		}

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.WarpDropdown(graphics, list));
	});

	public static final MenuItem TICK_FROZEN = MenuItem.text(ImIcons.FREEZE, ImText.info("Tick Frozen"));

	public static final MenuItem MAIN_MENU_BAR = MenuItem.root((graphics, list) -> {
		list.add(OPEN);
		list.add(CONFIG);
		list.add(DEBUG);
		list.add(SHOW);
		list.add(WARP);

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.MenuBar(graphics, list));

		if (!graphics.isReplay && graphics.inGame && graphics.mc.level.tickRateManager().isFrozen()) {
			list.add(TICK_FROZEN);
		}
	});

	public static void handle(Minecraft mc) {
		var graphics = new ImGraphics(mc);
		graphics.pushStack();
		graphics.setDefaultStyle();
		graphics.setNumberType(ImNumberType.DOUBLE);
		graphics.setNumberRange(null);

		if (graphics.adminPanel) {
			adminPanel(graphics);
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

			if (SoundEventImBuilder.soundModal(graphics, null).isAny() || !SoundEventImBuilder.PREVIEW_OPEN.get()) {
				showSounds = null;
			}
		}

		graphics.popStack();
	}

	public static void adminPanel(ImGraphics graphics) {
		var menuOpen = mainMenuOpen;
		mainMenuOpen = true;

		if (menuOpen) {
			MAIN_MENU_BAR.buildRoot(graphics, true);
		}

		OPEN_PANELS.removeIf(panel -> panel.handle(graphics));
	}
}
