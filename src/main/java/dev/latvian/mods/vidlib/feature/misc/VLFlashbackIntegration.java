package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.clock.ClockRenderer;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImNumberType;
import dev.latvian.mods.vidlib.feature.imgui.PropExplorerPanel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropListType;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.RecordedProp;
import dev.latvian.mods.vidlib.feature.structure.GhostStructure;
import imgui.ImGui;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VLFlashbackIntegration {
	public static final boolean ENABLED = ModList.get().isLoaded("flashback");

	private static int selectedProp = 0;
	private static PropListType selectedPropList = PropListType.LEVEL;
	private static boolean openSelectedPropPopup = false;
	public static final ImBoolean RECORD_PROPS = new ImBoolean(false);
	public static final Int2ObjectMap<RecordedProp> RECORDING_PROPS = new Int2ObjectLinkedOpenHashMap<>();
	public static final Int2ObjectMap<RecordedProp> RECORDED_PROPS = new Int2ObjectLinkedOpenHashMap<>();

	public static void init() {
		VidLib.LOGGER.info("Flashback integration loaded");
		FlashbackIntegration.CONFIG_SNAPSHOT.add(VLFlashbackIntegration::configSnapshot);
		FlashbackIntegration.GAME_SNAPSHOT.add(VLFlashbackIntegration::gameSnapshot);
		FlashbackIntegration.ENTITY_SNAPSHOT.add(VLFlashbackIntegration::entitySnapshot);
		FlashbackIntegration.ENTITY_MENU.add(VLFlashbackIntegration::entityMenu);
		FlashbackIntegration.VISUALS_MENU.add(VLFlashbackIntegration::visualsMenu);
		FlashbackIntegration.RENDER_FILTER_MENU.add(VLFlashbackIntegration::renderFilterMenu);
		FlashbackIntegration.EDITOR_STATE_LOADED.add(VLFlashbackIntegration::editorStateLoaded);
		FlashbackIntegration.EDITOR_STATE_SAVED.add(VLFlashbackIntegration::editorStateSaved);
		FlashbackIntegration.CLICK_TARGET.add(VLFlashbackIntegration::clickTarget);
		FlashbackIntegration.HANDLE_CLICK_TARGET.add(VLFlashbackIntegration::handleClickTarget);
		FlashbackIntegration.POPUPS.add(VLFlashbackIntegration::popups);
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

	private static void entityMenu(Entity entity) {
	}

	private static void visualsMenu() {
		var level = Minecraft.getInstance().level;

		ImGui.separator();
		ImGui.text("VidLib");
		ImGui.pushID("vidlib");
		ImGui.checkbox("Props", ClientProps.VISIBLE);

		if (ImGui.checkbox("Record Props [" + (RECORDED_PROPS.size() + RECORDING_PROPS.size()) + "]###record-props", RECORD_PROPS)) {

			if (RECORD_PROPS.get()) {
				RECORDED_PROPS.clear();

				for (var prop : level.getProps().levelProps) {
					VLFlashbackIntegration.RECORDING_PROPS.put(prop.id, new RecordedProp(prop.id, prop.type, prop.createdTime, 0L, prop.getDataJson(level.jsonOps())));
				}
			} else {
				var now = level.getGameTime();

				for (var r : RECORDING_PROPS.values()) {
					RECORDED_PROPS.put(r.id(), r.finish(now));
				}
			}

			RECORDING_PROPS.clear();
		}

		ImGui.checkbox("Physics Particles", PhysicsParticleManager.VISIBLE);
		ImGui.checkbox("Clocks", ClockRenderer.VISIBLE);
		ImGui.checkbox("Ghost Structures", GhostStructure.VISIBLE_CONFIG);
		ImGui.checkbox("Bloom", Bloom.VISIBLE);

		if (ImGui.button("Restore Bulk Removed Blocks###restore-bulk-removed-blocks")) {
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
		RECORDED_PROPS.clear();

		if (customData.has("vidlib:recorded_props")) {
			var arr = customData.getAsJsonArray("vidlib:recorded_props");

			for (var e : arr) {
				var obj = e.getAsJsonObject();
				var id = obj.get("id").getAsInt();
				var type = PropType.ALL.get().get(ResourceLocation.parse(obj.get("type").getAsString()));
				var spawn = obj.get("spawn").getAsLong();
				var remove = obj.get("remove").getAsLong();
				var data = obj.get("data").getAsJsonObject();

				if (type != null) {
					RECORDED_PROPS.put(id, new RecordedProp(id, type, spawn, remove, data));
				}
			}
		}
	}

	private static void editorStateSaved(JsonObject customData) {
		if (RECORDED_PROPS.isEmpty()) {
			customData.remove("vidlib:recorded_props");
		} else {
			var arr = new JsonArray();

			for (var r : RECORDED_PROPS.values()) {
				var obj = new JsonObject();
				obj.addProperty("id", r.id());
				obj.addProperty("type", r.type().id().toString());
				obj.addProperty("spawn", r.spawn());
				obj.addProperty("remove", r.remove());
				obj.add("data", r.data());
				arr.add(obj);
			}

			customData.add("vidlib:recorded_props", arr);
		}
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
		if (openSelectedPropPopup) {
			ImGui.openPopup("###vidlib-prop-popup");
			openSelectedPropPopup = false;
		}

		if (selectedProp != 0 && ImGui.beginPopup("###vidlib-prop-popup")) {
			var mc = Minecraft.getInstance();
			var propList = mc.level.getProps().propLists.get(selectedPropList);
			var prop = propList == null ? null : propList.get(selectedProp);

			if (prop != null) {
				PropExplorerPanel.OPEN_PROPS.add(prop.id);

				ImGui.text(prop.toString());
				var graphics = new ImGraphics(mc);
				graphics.pushStack();
				graphics.setDefaultStyle();
				graphics.setNumberType(ImNumberType.DOUBLE);
				graphics.setNumberRange(null);
				prop.imgui(graphics, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
				graphics.popStack();
			}

			ImGui.endPopup();
		}

		if (!ImGui.isPopupOpen("###vidlib-prop-popup")) {
			selectedProp = 0;
		}
	}
}
