package dev.latvian.mods.vidlib.integration.replay;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.replay.api.ReplayMarkerType;
import dev.latvian.mods.replay.api.event.RegisterReplaySessionDataEvent;
import dev.latvian.mods.replay.api.event.ReplayEntityMenuEvent;
import dev.latvian.mods.replay.api.event.ReplayGetClickTargetEvent;
import dev.latvian.mods.replay.api.event.ReplayHandleClickTargetEvent;
import dev.latvian.mods.replay.api.event.ReplayIconsEvent;
import dev.latvian.mods.replay.api.event.ReplayMenuBarEvent;
import dev.latvian.mods.replay.api.event.ReplayPopupEvent;
import dev.latvian.mods.replay.api.event.ReplayRenderFilterMenuEvent;
import dev.latvian.mods.replay.api.event.ReplaySessionClosedEvent;
import dev.latvian.mods.replay.api.event.ReplaySessionOpenedEvent;
import dev.latvian.mods.replay.api.event.ReplayStyleEvent;
import dev.latvian.mods.replay.api.event.ReplayVisualsMenuEvent;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibClientEventHandler;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.clock.ClockRenderer;
import dev.latvian.mods.vidlib.feature.data.DataMapOverrides;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.imgui.BuiltInImGui;
import dev.latvian.mods.vidlib.feature.imgui.EntityExplorerPanel;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.misc.EventMarkerPayload;
import dev.latvian.mods.vidlib.feature.misc.SyncPlayerTagsPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.prop.AddPropPayload;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropListType;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.RecordedProp;
import dev.latvian.mods.vidlib.feature.prop.RemovePropsPayload;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import dev.latvian.mods.vidlib.integration.FlashbackIntegration;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.world.level.ClipContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Set;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class VLReplayIntegration {
	public static int selectedProp = 0;
	public static JsonObject selectedPropData = null;
	public static PropListType selectedPropList = PropListType.LEVEL;
	public static boolean openSelectedPropPopup = false;
	public static int lastWidth = -1, lastHeight = -1;

	@SubscribeEvent
	public static void replaySessionOpened(ReplaySessionOpenedEvent event) {
		var mc = Minecraft.getInstance();
		var server = mc.getSingleplayerServer();
		var registryAccess = server.registryAccess();

		var dataMapOverrideBuilder = new DataMapOverrides.Builder();
		var recordedProps = new ArrayList<RecordedProp>();
		var recordingProps = new Int2ObjectLinkedOpenHashMap<RecordedProp>();

		long startTick = event.getSession().getFileInfo().getStartGameTick();

		for (var entry : event.getGamePackets()) {
			if (entry.packet() instanceof ClientboundCustomPayloadPacket c) {
				if (c.payload() instanceof VidLibPacketPayloadContainer w) {
					long now = w.remoteGameTime();

					switch (w.wrapped()) {
						case SyncServerDataPayload p -> {
							for (var u : p.update()) {
								if (u.key() != null) {
									dataMapOverrideBuilder.set(now, null, u.key(), u.value());
								}
							}
						}
						case SyncPlayerDataPayload p -> {
							for (var u : p.update()) {
								if (u.key() != null) {
									dataMapOverrideBuilder.set(now, p.player(), u.key(), u.value());
								}
							}
						}
						case SyncPlayerTagsPayload p -> dataMapOverrideBuilder.set(now, p.player(), InternalPlayerData.PLAYER_TAGS, Set.copyOf(p.tags()));
						case AddPropPayload p -> {
							var map = new IdentityHashMap<PropData<?, ?>, Object>();
							try {
								p.type().readReplayUpdate(now, p.id(), registryAccess, p.update(), true, map::put);
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							var old = recordingProps.get(p.id());

							if (old != null) {
								old.data.putAll(map);
							} else {
								var rp = new RecordedProp(p.id(), p.type());
								rp.spawn = p.createdTime();
								rp.data.putAll(map);
								recordingProps.put(p.id(), rp);
							}

						}
						case RemovePropsPayload p -> {
							for (var id : p.ids()) {
								var prop = recordingProps.remove(id.intValue());

								if (prop != null) {
									prop.remove = now;
									recordedProps.add(prop);
								}
							}
						}
						case EventMarkerPayload p -> {
							var data = ClientGameEngine.INSTANCE.handleReplayMarker(entry.dimension(), p.event(), p.tag().orElse(null));

							if (data != null) {
								event.addMarker((int) (now - startTick), ReplayMarkerType.EVENT_MARKER, data);
							}
						}
						case null, default -> {
						}
					}
				}
			}
		}

		long endTick = event.getSession().getFileInfo().getEndGameTick();

		for (var prop : recordingProps.values()) {
			prop.remove = endTick;
			recordedProps.add(prop);
		}

		recordedProps.sort(Comparator.comparingLong(a -> a.spawn));

		VidLib.LOGGER.info("Flashback props: " + recordedProps.size());

		DataMapOverrides.INSTANCE = dataMapOverrideBuilder.build();
		RecordedProp.LIST = recordedProps;
		RecordedProp.MAP = new Int2ObjectOpenHashMap<>();

		for (var prop : recordedProps) {
			RecordedProp.MAP.put(prop.id, prop);
		}
	}

	@SubscribeEvent
	public static void replaySessionClosed(ReplaySessionClosedEvent event) {
		DataMapOverrides.INSTANCE = null;
		RecordedProp.MAP = null;
		RecordedProp.LIST = null;
	}

	@SubscribeEvent
	public static void menuBar(ReplayMenuBarEvent event) {
		ImGui.separator();
		BuiltInImGui.MAIN_MENU_BAR.buildMenuBar(event.getGraphics(), false);
	}

	@SubscribeEvent
	public static void entityMenu(ReplayEntityMenuEvent event) {
		ImGuiUtils.separatorWithText("VidLib");
		var mc = Minecraft.getInstance();
		EntityExplorerPanel.imgui(event.getGraphics(), event.getEntity(), mc.getDeltaTracker().getGameTimeDeltaPartialTick(event.getEntity() == mc.player));
	}

	@SubscribeEvent
	public static void visualsMenu(ReplayVisualsMenuEvent event) {
		ImGuiUtils.separatorWithText("VidLib");

		ImGui.pushID("vidlib");
		ImGui.pushID("###flags");
		ImGui.checkbox("Props###props", ClientProps.VISIBLE);
		ImGui.checkbox("Physics Particles###physics-particles", PhysicsParticleManager.VISIBLE);
		ImGui.checkbox("Clocks###clocks", ClockRenderer.VISIBLE);
		ImGui.checkbox("Bloom###bloom", Bloom.VISIBLE);
		ImGui.checkbox("Ghost Structures###ghost-structures", GhostStructure.VISIBLE_CONFIG);
		ImGui.popID();

		ImGuiUtils.separatorWithText("Player Pins");
		ImGui.pushID("###pins");
		Pins.fbVisualsMenu(event.getGraphics());
		ImGui.popID();

		ImGui.popID();
	}

	@SubscribeEvent
	public static void renderFilterMenu(ReplayRenderFilterMenuEvent event) {
		if (ImGui.beginTabItem("Props")) {
			if (ImGui.beginListBox("###props")) {
				for (var propType : PropType.SORTED.get()) {
					boolean visible = !ClientProps.HIDDEN_PROP_TYPES.contains(propType);

					if (ImGui.selectable((visible ? ImIcons.VISIBLE : ImIcons.INVISIBLE) + " " + propType.id().toString(), visible)) {
						if (visible) {
							ClientProps.HIDDEN_PROP_TYPES.add(propType);
						} else {
							ClientProps.HIDDEN_PROP_TYPES.remove(propType);
						}
					}
				}

				ImGui.endListBox();
			}

			ImGui.endTabItem();
		}
	}

	@SubscribeEvent
	public static void registerReplaySessionData(RegisterReplaySessionDataEvent event) {
		event.register(new PinReplaySessionData());
	}

	@SubscribeEvent
	public static void clickTarget(ReplayGetClickTargetEvent event) {
		var mc = Minecraft.getInstance();
		var ctx = new ClipContext(event.getFrom(), event.getTo(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player);
		event.setHitResult(mc.level.getProps().clip(ctx, true));
	}

	@SubscribeEvent
	public static void handleClickTarget(ReplayHandleClickTargetEvent event) {
		if (event.getHitResult() instanceof PropHitResult propResult) {
			selectedProp = propResult.prop.id;
			selectedPropData = propResult.prop.getDataJson(JsonOps.INSTANCE);
			selectedPropList = propResult.prop.spawnType.listType;
			openSelectedPropPopup = true;
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void popups(ReplayPopupEvent event) {
		var mc = Minecraft.getInstance();

		if (VidLibClientEventHandler.clientLoaded && mc.level != null && mc.level.isReplayLevel()) {
			BuiltInImGui.handle(event.getGraphics());
		}

		if (openSelectedPropPopup) {
			ImGui.openPopup("###vidlib-prop-popup");
			openSelectedPropPopup = false;
		}

		if (selectedProp != 0 && ImGui.beginPopup("###vidlib-prop-popup", ImGuiWindowFlags.AlwaysAutoResize)) {
			var propList = mc.level.getProps().propLists.get(selectedPropList);
			var prop = propList == null ? null : propList.get(selectedProp);

			if (prop != null) {
				ClientProps.OPEN_PROPS.add(prop.id);
				ImGui.text(prop.toString());
				prop.imgui(event.getGraphics(), mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
			}

			ImGui.endPopup();
		}

		if (!ImGui.isPopupOpen("###vidlib-prop-popup")) {
			var propList = mc.level.getProps().propLists.get(selectedPropList);
			var prop = propList == null ? null : propList.get(selectedProp);
			if (prop != null) {
				var data = prop.getDataJson(JsonOps.INSTANCE);
				if (!selectedPropData.equals(data)) {
					FlashbackIntegration.MAKE_PROP_KEYFRAMES.add(prop);
				}
			}
			selectedProp = 0;
		}

		int w = mc.getWindow().getWidth();
		int h = mc.getWindow().getHeight();
		int scw = mc.getWindow().getGuiScaledWidth();
		int sch = mc.getWindow().getGuiScaledHeight();

		// Fix flashback lagging extremely after window is minimized
		if (scw > w || sch > h || lastWidth != scw || lastHeight != sch) {
			mc.resizeDisplay();
			lastWidth = scw;
			lastHeight = sch;
		}
	}

	@SubscribeEvent
	public static void icons(ReplayIconsEvent event) {
		for (var icon : ImIcons.VALUES) {
			if (icon.icon != 0) {
				event.add(icon.icon);
			}
		}

		for (var c : ImIcons.EXTRA_ICONS.get()) {
			event.add(c.toChar());
		}
	}

	@SubscribeEvent
	public static void style(ReplayStyleEvent event) {
		ImGraphics.setFullDefaultStyle(event.getStyle());
	}
}
