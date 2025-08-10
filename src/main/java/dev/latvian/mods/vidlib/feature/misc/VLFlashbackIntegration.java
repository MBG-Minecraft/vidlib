package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonObject;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibClientEventHandler;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.clock.ClockRenderer;
import dev.latvian.mods.vidlib.feature.data.DataMapOverrides;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.imgui.BuiltInImGui;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.PropExplorerPanel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.prop.AddPropPayload;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropListType;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.RecordedProp;
import dev.latvian.mods.vidlib.feature.prop.RemovePropsPayload;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import imgui.ImGui;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VLFlashbackIntegration {
	public static final boolean ENABLED = ModList.get().isLoaded("flashback");

	private static int selectedProp = 0;
	private static PropListType selectedPropList = PropListType.LEVEL;
	private static boolean openSelectedPropPopup = false;

	public static void init() {
		VidLib.LOGGER.info("Flashback integration loaded");
		FlashbackIntegration.INITIALIZED.add(VLFlashbackIntegration::initialized);
		FlashbackIntegration.CLEANUP.add(VLFlashbackIntegration::cleanup);
		FlashbackIntegration.CONFIG_SNAPSHOT.add(VLFlashbackIntegration::configSnapshot);
		FlashbackIntegration.GAME_SNAPSHOT.add(VLFlashbackIntegration::gameSnapshot);
		FlashbackIntegration.ENTITY_SNAPSHOT.add(VLFlashbackIntegration::entitySnapshot);
		FlashbackIntegration.MENU_BAR.add(VLFlashbackIntegration::menuBar);
		FlashbackIntegration.ENTITY_MENU.add(VLFlashbackIntegration::entityMenu);
		FlashbackIntegration.VISUALS_MENU.add(VLFlashbackIntegration::visualsMenu);
		FlashbackIntegration.RENDER_FILTER_MENU.add(VLFlashbackIntegration::renderFilterMenu);
		FlashbackIntegration.EDITOR_STATE_LOADED.add(VLFlashbackIntegration::editorStateLoaded);
		FlashbackIntegration.EDITOR_STATE_SAVED.add(VLFlashbackIntegration::editorStateSaved);
		FlashbackIntegration.CLICK_TARGET.add(VLFlashbackIntegration::clickTarget);
		FlashbackIntegration.HANDLE_CLICK_TARGET.add(VLFlashbackIntegration::handleClickTarget);
		FlashbackIntegration.POPUPS.add(VLFlashbackIntegration::popups);
		FlashbackIntegration.ICONS.add(VLFlashbackIntegration::icons);
	}

	private static void initialized(List<Packet<? super ClientConfigurationPacketListener>> configPackets, List<LongObjectPair<Packet<? super ClientGamePacketListener>>> gamePackets) {
		var mc = Minecraft.getInstance();
		var server = mc.getSingleplayerServer();
		var registryAccess = server.registryAccess();

		var dataMapOverrideBuilder = new DataMapOverrides.Builder();
		var recorderProps = new Int2ObjectLinkedOpenHashMap<RecordedProp>();
		var recordingProps = new Int2ObjectLinkedOpenHashMap<RecordedProp>();

		for (var entry : gamePackets) {
			if (entry.value() instanceof ClientboundCustomPayloadPacket c) {
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
						case SyncPlayerTagsPayload p -> dataMapOverrideBuilder.set(now, p.player(), DataMapOverrides.PLAYER_TAGS, Set.copyOf(p.tags()));
						case AddPropPayload p -> {
							var map = new IdentityHashMap<PropData<?, ?>, Object>();
							p.type().readUpdate(p.id(), registryAccess, p.update(), true, map::put);
							recordingProps.put(p.id(), new RecordedProp(p.id(), p.type(), p.createdTime(), 0L, Map.copyOf(map)));
						}
						case RemovePropsPayload p -> {
							for (var id : p.ids()) {
								var prop = recordingProps.remove(id.intValue());

								if (prop != null) {
									recorderProps.put(prop.id(), prop.finish(now));
								}
							}
						}
						case null, default -> {
						}
					}
				}
			}
		}

		long endTick = FlashbackIntegration.getEndTick();

		for (var prop : recordingProps.values()) {
			recorderProps.put(prop.id(), prop.finish(endTick));
		}

		VidLib.LOGGER.info("Flashback props: " + recorderProps.size());

		DataMapOverrides.INSTANCE = dataMapOverrideBuilder.build();
		RecordedProp.INSTANCE = recorderProps;
	}

	private static void cleanup() {
		DataMapOverrides.INSTANCE = null;
		RecordedProp.INSTANCE = null;
	}

	private static void configSnapshot(List<Packet<? super ClientConfigurationPacketListener>> packets) {
		// VidLib.LOGGER.info("Flashback Config snapshot");
	}

	private static void gameSnapshot(List<Packet<? super ClientGamePacketListener>> packets) {
		VidLib.LOGGER.info("Flashback Game snapshot");

		var mc = Minecraft.getInstance();
		var packets2 = new S2CPacketBundleBuilder(mc.level);
		mc.player.vl$sessionData().sync(packets2, mc.player, 1);
		packets2.sendUnbundled(packets::add);
	}

	private static void entitySnapshot(Entity entity, List<Packet<? super ClientGamePacketListener>> packets) {
		var packets2 = new S2CPacketBundleBuilder(Minecraft.getInstance().level);
		entity.replaySnapshot(packets2);
		packets2.sendUnbundled(packets::add);
	}

	private static void menuBar() {
		ImGui.separator();
		var graphics = new ImGraphics(Minecraft.getInstance());
		graphics.pushRootStack();
		BuiltInImGui.MAIN_MENU_BAR.buildRoot(graphics, false);
		graphics.popStack();
	}

	private static void entityMenu(Entity entity) {
		ImGuiUtils.separatorWithText("VidLib");
		var mc = Minecraft.getInstance();
		var graphics = new ImGraphics(mc);
		graphics.pushRootStack();
		entity.imgui(graphics, mc.getDeltaTracker().getGameTimeDeltaPartialTick(entity == mc.player));
		graphics.popStack();
	}

	private static void visualsMenu() {
		var mc = Minecraft.getInstance();
		var level = mc.level;

		ImGuiUtils.separatorWithText("VidLib");

		ImGui.pushID("vidlib");
		ImGui.checkbox("Props", ClientProps.VISIBLE);
		ImGui.checkbox("Physics Particles", PhysicsParticleManager.VISIBLE);
		ImGui.checkbox("Clocks", ClockRenderer.VISIBLE);
		ImGui.checkbox("Ghost Structures", GhostStructure.VISIBLE_CONFIG);
		ImGui.checkbox("Bloom", Bloom.VISIBLE);

		if (!level.vl$getUndoableModifications().isEmpty() && ImGui.button("Restore Bulk Removed Blocks###restore-bulk-removed-blocks")) {
			level.undoAllFutureModifications(true);
		}

		ImGui.popID();
	}

	private static void renderFilterMenu() {
		if (ImGui.beginTabItem("Props")) {
			if (ImGui.beginListBox("###props")) {
				for (var propType : PropType.ALL.get().values()) {
					boolean visible = !PropExplorerPanel.HIDDEN_PROP_TYPES.contains(propType);

					if (ImGui.selectable((visible ? ImIcons.VISIBLE : ImIcons.INVISIBLE) + " " + propType.id().toString(), visible)) {
						if (visible) {
							PropExplorerPanel.HIDDEN_PROP_TYPES.add(propType);
						} else {
							PropExplorerPanel.HIDDEN_PROP_TYPES.remove(propType);
						}
					}
				}

				ImGui.endListBox();
			}

			ImGui.endTabItem();
		}
	}

	private static void editorStateLoaded(JsonObject customData) {
	}

	private static void editorStateSaved(JsonObject customData) {
	}

	@Nullable
	private static HitResult clickTarget(Vec3 from, Vec3 to) {
		var mc = Minecraft.getInstance();
		var ctx = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player);
		var result = mc.level.getProps().clip(ctx, true);
		return result;
	}

	private static boolean handleClickTarget(HitResult result) {
		if (result instanceof PropHitResult propResult) {
			selectedProp = propResult.prop.id;
			selectedPropList = propResult.prop.spawnType.listType;
			openSelectedPropPopup = true;
			return true;
		}

		return false;
	}

	private static void popups() {
		var mc = Minecraft.getInstance();
		var graphics = new ImGraphics(mc);
		graphics.pushRootStack();

		if (VidLibClientEventHandler.clientLoaded && mc.level != null && mc.level.isReplayLevel()) {
			BuiltInImGui.handle(graphics);
		}

		if (openSelectedPropPopup) {
			ImGui.openPopup("###vidlib-prop-popup");
			openSelectedPropPopup = false;
		}

		if (selectedProp != 0 && ImGui.beginPopup("###vidlib-prop-popup")) {
			var propList = mc.level.getProps().propLists.get(selectedPropList);
			var prop = propList == null ? null : propList.get(selectedProp);

			if (prop != null) {
				PropExplorerPanel.OPEN_PROPS.add(prop.id);
				ImGui.text(prop.toString());
				prop.imgui(graphics, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
			}

			ImGui.endPopup();
		}

		if (!ImGui.isPopupOpen("###vidlib-prop-popup")) {
			selectedProp = 0;
		}

		graphics.popStack();
	}

	private static void icons(CharConsumer chars) {
		for (var icon : ImIcons.VALUES) {
			chars.accept(icon.icon);
		}

		for (var c : ImIcons.EXTRA_ICONS.get()) {
			chars.accept(c.toChar());
		}
	}
}
