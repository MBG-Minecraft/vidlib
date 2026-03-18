package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.platform.TextureUtil;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.canvas.CanvasPanel;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.clock.ClockRenderer;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneBuilderPanel;
import dev.latvian.mods.vidlib.feature.decal.DecalPanel;
import dev.latvian.mods.vidlib.feature.environment.FluidPlanePanel;
import dev.latvian.mods.vidlib.feature.environment.MapTextureOverridePanel;
import dev.latvian.mods.vidlib.feature.environment.WorldBorderPanel;
import dev.latvian.mods.vidlib.feature.gallery.LowQualityPlayerBodies;
import dev.latvian.mods.vidlib.feature.gallery.PlayerBodies;
import dev.latvian.mods.vidlib.feature.gallery.PlayerHeads;
import dev.latvian.mods.vidlib.feature.gallery.PlayerSkins;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.net.PacketDebuggerPanel;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueueImGui;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectPanel;
import dev.latvian.mods.vidlib.feature.screeneffect.chromaticaberration.ChromaticAberrationPanel;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldPanel;
import dev.latvian.mods.vidlib.feature.skybox.ClientSkybox;
import dev.latvian.mods.vidlib.feature.sound.SoundEventImBuilder;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.feature.waypoint.ClientWaypoints;
import dev.latvian.mods.vidlib.util.LevelOfDetailValue;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuiltInImGui {
	public static final Map<String, Panel> OPEN_PANELS = new LinkedHashMap<>();
	public static final ImBoolean SHOW_STACK_TOOL = new ImBoolean(false);
	public static final ImBoolean SHOW_STYLE_EDITOR_TOOL = new ImBoolean(false);
	public static final ImBoolean SHOW_BOTTOM_INFO_BAR = new ImBoolean(true);
	public static Boolean showSounds = null;


	public static final MenuItem OPEN = MenuItem.menu(ImIcons.OPEN, "Open", (graphics, list) -> {
		list.add(MenuItem.item(ImIcons.MEMORY, "Memory Usage", MemoryUsagePanel.INSTANCE));
		list.add(MenuItem.item(ImIcons.SLASH, "Command History", CommandHistoryPanel.INSTANCE));
		list.add(MenuItem.item(ImIcons.TIMELAPSE, "Server Stopwatch", GlobalStopwatchPanel.INSTANCE).enabled(graphics.inGame));
		list.add(MenuItem.item(ImIcons.TIMELAPSE, "New Stopwatch", g -> StopwatchPanel.openNew()));
		list.add(MenuItem.menu(ImIcons.APERTURE, "Canvas", CanvasPanel::menu));
		list.add(MenuItem.item(ImIcons.MUSIC_NOTE, "Sounds", BuiltInImGui.showSounds != null, g -> BuiltInImGui.showSounds = false));
		list.add(MenuItem.SEPARATOR);
		list.add(MenuItem.item(ImIcons.CAMERA, "Cutscene Builder", CutsceneBuilderPanel.INSTANCE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.PAW, "Entity Explorer", EntityExplorerPanel.INSTANCE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.SEARCH, "Prop Explorer", PropExplorerPanel.INSTANCE).enabled(graphics.isAdmin));

		if (graphics.isReplay) {
			list.add(MenuItem.item(ImIcons.SEARCH, "Replay Prop Explorer", ReplayPropExplorerPanel.INSTANCE));
		}

		list.add(MenuItem.item(ImIcons.LAYERS, "Decals", DecalPanel.INSTANCE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.BLUR, "Screen Effects", ScreenEffectPanel.INSTANCE).enabled(graphics.isAdmin));

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.OpenDropdown(graphics, list));

		list.add(MenuItem.SEPARATOR);
		list.add(MenuItem.item(ImIcons.FRAMED_CUBE, "Debug Widgets", DebugWidgetPanel.INSTANCE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.BUG, "Packet Debugger", PacketDebuggerPanel.INSTANCE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.MEMORY, "ID Stack Tool", SHOW_STACK_TOOL));
		list.add(MenuItem.item(ImIcons.EDIT, "Style Editor Tool", SHOW_STYLE_EDITOR_TOOL));
	});

	public static final MenuItem CONFIG = MenuItem.menu(ImIcons.SETTINGS, "Config", (graphics, list) -> {
		list.add(MenuItem.item(ImIcons.WRENCH, "Server Data", ServerDataConfigPanel.INSTANCE).enabled(graphics.isAdmin));

		if (graphics.player != null) {
			list.add(MenuItem.item(ImIcons.WRENCH, "Player Data (Self)", g1 -> new PlayerDataConfigPanel(graphics.player.getGameProfile(), graphics.player.vl$sessionData().dataMap).open()).enabled(graphics.isAdmin));
		}

		list.add(ClientSkybox.MENU_ITEM.enabled(graphics.isAdmin));
		list.add(DepthOfFieldPanel.MENU_ITEM.enabled(graphics.isSinglePlayer));
		list.add(ChromaticAberrationPanel.MENU_ITEM.enabled(graphics.isSinglePlayer));
		list.add(FluidPlanePanel.MENU_ITEM.enabled(graphics.isSinglePlayer));
		list.add(WorldBorderPanel.MENU_ITEM.enabled(graphics.isSinglePlayer));
		list.add(MapTextureOverridePanel.MENU_ITEM.enabled(graphics.isSinglePlayer));

		list.add(MenuItem.menu(ImIcons.VISIBLE, "Level of Detail", (g1, menuItems) -> {
			menuItems.add(MenuItem.menu(ImIcons.SHIELD, "Player Armor", LevelOfDetailValue.PLAYER_ARMOR));
			menuItems.add(MenuItem.item(ImIcons.SHIELD, "Player Headwear", MiscClientUtils.PLAYER_HEADWEAR).remainOpen(true));
			menuItems.add(MenuItem.menu(ImIcons.SWORDS, "Held Item", LevelOfDetailValue.HELD_ITEM));
			menuItems.add(MenuItem.menu(ImIcons.SHIRT, "Clothing", LevelOfDetailValue.CLOTHING));
			menuItems.add(MenuItem.menu(ImIcons.PAW, "Entity Details", LevelOfDetailValue.ENTITY_DETAILS));
			menuItems.add(MenuItem.menu(ImIcons.SHIELD, "Entity Armor", LevelOfDetailValue.ENTITY_ARMOR));
			menuItems.add(MenuItem.menu(ImIcons.FIRE, "Block Entities", LevelOfDetailValue.BLOCK_ENTITIES));
		}));

		if (!graphics.isReplay) {
			list.add(Pins.MENU_ITEM.enabled(graphics.isAdmin));
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

		list.add(MenuItem.item(ImIcons.RELOAD, "Reload Shaders", g -> MiscClientUtils.reloadShaders(g.mc)));

		list.add(MenuItem.item(ImIcons.STOP, "Stop all Sounds", g -> g.mc.getSoundManager().stop()));

		list.add(MenuItem.item(ImIcons.DATABASE, "Dump Textures", g -> {
			var gameDir = PlatformHelper.CURRENT.getGameDirectory().toAbsolutePath();
			var textures = TextureUtil.getDebugTexturePath(gameDir);
			g.mc.getTextureManager().dumpAllSheets(textures);
			g.mc.tell(Component.translatableEscape("debug.dump_dynamic_textures", Component.literal(gameDir.relativize(textures).toString())
				.withStyle(ChatFormatting.UNDERLINE)
				.withStyle(s -> s.withClickEvent(new ClickEvent.OpenFile(textures)))
			));
		}).enabled(graphics.isAdmin));

		list.add(MenuItem.item(ImIcons.RELOAD, "Clear Skin Cache", g -> {
			boolean reload = false;

			for (var gallery : List.of(PlayerSkins.GALLERY, PlayerHeads.GALLERY, PlayerBodies.GALLERY, LowQualityPlayerBodies.GALLERY)) {
				for (var value : gallery.images.values()) {
					if (value.deleteFile()) {
						reload = true;
					}
				}

				gallery.images.clear();
			}

			if (reload) {
				g.mc.reloadResourcePacks();
			}
		}));

		list.add(MenuItem.item(ImIcons.LEAF, "JVM Threads", JVMThreadsPanel.INSTANCE));

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

		list.add(MenuItem.item(ImIcons.FLAG, "Props", ClientProps.VISIBLE).enabled(graphics.isAdmin));

		if (ClientProps.VISIBLE.get()) {
			list.add(MenuItem.menu(ImIcons.FLAG, "Prop Types", (graphics2, propTypes) -> {
				for (var type : PropType.SORTED.get()) {
					propTypes.add(MenuItem.item(type.id().toString(), !ClientProps.HIDDEN_PROP_TYPES.contains(type), graphics3 -> {
						if (ClientProps.HIDDEN_PROP_TYPES.contains(type)) {
							ClientProps.HIDDEN_PROP_TYPES.remove(type);
						} else {
							ClientProps.HIDDEN_PROP_TYPES.add(type);
						}
					}));
				}
			}).enabled(graphics.isAdmin));
		}

		list.add(MenuItem.item(ImIcons.STAR, "Physics Particles", PhysicsParticleManager.VISIBLE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.TIMELAPSE, "Clocks", ClockRenderer.VISIBLE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.SUN, "Bloom", Bloom.VISIBLE).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.FRAMED_CUBE, "Ghost Structures", GhostStructure.VISIBLE_CONFIG).enabled(graphics.isAdmin));
		list.add(MenuItem.item(ImIcons.LOCATION, "Waypoints", ClientWaypoints.VISIBLE).enabled(graphics.inGame));

		list.add(MenuItem.SEPARATOR);

		list.add(MenuItem.item(ImIcons.LOCATION, "Coordinates", VidLibClientOptions.getShowCoordinates(), g -> VidLibClientOptions.setShowCoordinates(!VidLibClientOptions.getShowCoordinates())).enabled(graphics.isAdmin));

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.ShowDropdown(graphics, list));
	}).remainOpen(true);

	public static final MenuItem WARP_TO_DIMENSIONS = MenuItem.menu(ImIcons.WORLD, "Dimension", (g1, list1) -> {
		var registry = g1.mc.player.connection.levels();

		for (var dimension : registry) {
			list1.add(MenuItem.item(dimension.location().toString(), g -> g.mc.runClientCommand("execute in " + dimension.location() + " run tp @s ~ ~ ~")).disabled(g1.isReplay));
		}
	});

	public static final MenuItem WARP = MenuItem.menu(ImIcons.LOCATION, "Warp", (graphics, list) -> {
		for (var location : CommonGameEngine.INSTANCE.getWarpLocations()) {
			if (!location.admin() || graphics.isAdmin) {
				list.add(MenuItem.item(
					ImIcons.LOCATION,
					location.displayName(),
					location.pos().apply(graphics.mc).closerToCenterThan(graphics.player.position(), 100D),
					g -> g.mc.runClientCommand("warp " + location.id())
				));
			}
		}

		if (graphics.inGame && !graphics.isReplay) {
			// list.add(WARP_TO_DIMENSIONS.enabled(graphics.isAdmin));
		}

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.WarpDropdown(graphics, list));
	});

	public static final MenuItem MAIN_MENU_BAR = MenuItem.root((graphics, list) -> {
		if (ClientGameEngine.INSTANCE.imGuiOpenMenu(graphics)) {
			list.add(OPEN);
		}

		if (ClientGameEngine.INSTANCE.imGuiConfigMenu(graphics)) {
			list.add(CONFIG);
		}

		if (ClientGameEngine.INSTANCE.imGuiDebugMenu(graphics)) {
			list.add(DEBUG);
		}

		if (ClientGameEngine.INSTANCE.imGuiShowMenu(graphics)) {
			list.add(SHOW);
		}

		if (ClientGameEngine.INSTANCE.imGuiWarpMenu(graphics)) {
			list.add(WARP);
		}

		NeoForge.EVENT_BUS.post(new AdminPanelEvent.MenuBar(graphics, list));
	});

	public static void handle(ImGraphics graphics) {
		graphics.pushStack();
		graphics.setWindowBorderSize(0F);
		graphics.setWindowPadding(0F, 0F);
		graphics.setWindowRounding(0F);
		graphics.setFramePadding(0F, 2F);
		graphics.setWindowMinSize(0F, 0F);
		graphics.setItemSpacing(6F, 0F);
		graphics.setFrameBorderSize(0F);
		graphics.setStyleCol(ImGuiCol.WindowBg, Color.BLACK);
		graphics.setStyleCol(ImGuiCol.MenuBarBg, Color.BLACK);

		ImGuiHooks.mainMenuBarHeight = 0F;
		boolean topMainMenu = !ClientGameEngine.DISABLE_IMGUI && ClientGameEngine.INSTANCE.hasTopInfoBar(graphics.mc);

		if (topMainMenu && !graphics.isReplay && ImGui.beginMainMenuBar()) {
			ImGuiHooks.mainMenuBarHeight = ImGui.getWindowSize().y;
			ClientGameEngine.INSTANCE.topInfoBarPre(graphics, ImGuiHooks.mainMenuBarHeight);
			MAIN_MENU_BAR.buildMenuBar(graphics, true);
			ImGui.separator();
			ClientGameEngine.INSTANCE.topInfoBar(graphics, ImGuiHooks.mainMenuBarHeight);
			topMainMenu = false;
			ImGui.endMainMenuBar();
		}

		if (topMainMenu && ImGui.beginMainMenuBar()) {
			ImGuiHooks.mainMenuBarHeight = ImGui.getWindowSize().y;
			ClientGameEngine.INSTANCE.topInfoBarPre(graphics, ImGuiHooks.mainMenuBarHeight);
			ClientGameEngine.INSTANCE.topInfoBar(graphics, ImGuiHooks.mainMenuBarHeight);
			ImGui.endMainMenuBar();
		}

		float h = (graphics.isReplay && (graphics.isExportingReplay || !SHOW_BOTTOM_INFO_BAR.get())) || !ClientGameEngine.INSTANCE.hasBottomInfoBar(graphics.mc) ? 0F : ImGuiHooks.mainMenuBarHeight;

		if (h > 0F && ImGuiHooks.mainViewport != null && ImGuiHooks.centralDockNode != null) {
			var centralNodePos = ImGuiHooks.centralDockNode.getPos();
			var centralNodeSize = ImGuiHooks.centralDockNode.getSize();

			graphics.pushStack();
			graphics.copyStyleColFrom(ImGuiCol.WindowBg, ImGuiCol.MenuBarBg);

			int flags = ImGuiWindowFlags.NoSavedSettings
				| ImGuiWindowFlags.MenuBar
				| ImGuiWindowFlags.NoMove
				| ImGuiWindowFlags.NoDocking
				| ImGuiWindowFlags.NoNav
				| ImGuiWindowFlags.NoDecoration;

			ImGui.setNextWindowPos(centralNodePos.x, centralNodePos.y + centralNodeSize.y - h);
			ImGui.setNextWindowSize(centralNodeSize.x, h);

			if (ImGui.begin("###bottom-info-bar", flags)) {
				if (ImGui.beginMenuBar()) {
					ClientGameEngine.INSTANCE.bottomInfoBar(graphics, h);
					ImGui.endMenuBar();
				}
			}

			ImGui.end();

			graphics.popStack();
		}

		graphics.popStack();

		OPEN_PANELS.values().removeIf(panel -> panel.handle(graphics));

		ProgressQueueImGui.handle(graphics);

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
	}
}
