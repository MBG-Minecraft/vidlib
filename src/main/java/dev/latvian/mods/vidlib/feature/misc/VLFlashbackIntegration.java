package dev.latvian.mods.vidlib.feature.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibClientEventHandler;
import dev.latvian.mods.vidlib.feature.bloom.Bloom;
import dev.latvian.mods.vidlib.feature.clock.ClockRenderer;
import dev.latvian.mods.vidlib.feature.data.DataMapOverrides;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.data.SyncServerDataPayload;
import dev.latvian.mods.vidlib.feature.imgui.BuiltInImGui;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiAPI;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.pin.Pin;
import dev.latvian.mods.vidlib.feature.pin.Pins;
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
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
		FlashbackIntegration.STYLE.add(ImGraphics::setFullDefaultStyle);

		ImGuiAPI.HIDE.add(FlashbackIntegration.IN_EXPORTING);
	}

	private static void initialized(List<Packet<? super ClientConfigurationPacketListener>> configPackets, List<LongObjectPair<Packet<? super ClientGamePacketListener>>> gamePackets) {
		var mc = Minecraft.getInstance();
		var server = mc.getSingleplayerServer();
		var registryAccess = server.registryAccess();

		var dataMapOverrideBuilder = new DataMapOverrides.Builder();
		var recordedProps = new ArrayList<RecordedProp>();
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
							try {
								p.type().readUpdate(p.id(), registryAccess, p.update(), true, map::put);
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
						case null, default -> {
						}
					}
				}
			}
		}

		long endTick = FlashbackIntegration.getEndTick();

		for (var prop : recordingProps.values()) {
			prop.remove = endTick;
			recordedProps.add(prop);
		}

		VidLib.LOGGER.info("Flashback props: " + recordedProps.size());

		DataMapOverrides.INSTANCE = dataMapOverrideBuilder.build();
		RecordedProp.LIST = recordedProps;
		RecordedProp.MAP = new Int2ObjectOpenHashMap<>();

		for (var prop : recordedProps) {
			RecordedProp.MAP.put(prop.id, prop);
		}
	}

	private static void cleanup() {
		DataMapOverrides.INSTANCE = null;
		RecordedProp.MAP = null;
		RecordedProp.LIST = null;
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

	private static void menuBar(ImGraphics graphics) {
		ImGui.separator();
		BuiltInImGui.MAIN_MENU_BAR.buildRoot(graphics, false);
	}

	private static void entityMenu(ImGraphics graphics, Entity entity) {
		ImGuiUtils.separatorWithText("VidLib");
		var mc = Minecraft.getInstance();
		entity.imgui(graphics, mc.getDeltaTracker().getGameTimeDeltaPartialTick(entity == mc.player));
	}

	private static void visualsMenu(ImGraphics graphics) {
		ImGuiUtils.separatorWithText("VidLib");

		ImGui.pushID("vidlib");
		ImGui.checkbox("Props", ClientProps.VISIBLE);
		ImGui.checkbox("Physics Particles", PhysicsParticleManager.VISIBLE);
		ImGui.checkbox("Clocks", ClockRenderer.VISIBLE);
		ImGui.checkbox("Ghost Structures", GhostStructure.VISIBLE_CONFIG);
		ImGui.checkbox("Bloom", Bloom.VISIBLE);

		ImGuiUtils.separatorWithText("Player Pins");
		ImGui.checkbox("Enabled###pins-enabled", Pins.ENABLED);
		ImGui.sliderFloat("Pin Size###pin-size", Pins.PIN_SIZE.getData(), 0F, 1024F);
		ImGui.sliderFloat("Pin Offset###pin-offset", Pins.PIN_OFFSET.getData(), 0F, 1F);
		ImGui.sliderInt("Pin Alpha###pin-alpha", Pins.PIN_ALPHA.getData(), 1, 255);

		ImGui.popID();
	}

	private static void renderFilterMenu(ImGraphics graphics) {
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

	private static void editorStateLoaded(JsonObject customData) {
		var mc = Minecraft.getInstance();
		Pins.PINS.clear();

		if (customData.has("vidlib:pins")) {
			var pins = customData.getAsJsonArray("vidlib:pins");

			for (var e : pins) {
				if (e instanceof JsonObject pinJson) {
					try {
						var uuid = UUID.fromString(pinJson.get("uuid").getAsString());
						var name = pinJson.get("name").getAsString();
						var enabled = pinJson.get("enabled").getAsBoolean();
						var pathString = pinJson.get("path").getAsString();

						var path = Path.of(pathString);

						if (Files.exists(path)) {
							mc.execute(() -> {
								try (var stream = Files.newInputStream(path)) {
									var resourceLocation = VidLib.id("textures/vidlib/cache/pins/" + UndashedUuid.toString(uuid) + ".png");
									var image = NativeImage.read(stream);
									var texture = new DynamicTexture(uuid::toString, image);
									mc.getTextureManager().register(resourceLocation, texture);
									Pins.PINS.put(uuid, new Pin(uuid, name, new ImBoolean(enabled), resourceLocation, pathString));
								} catch (IOException ignore) {
								}
							});
						}
					} catch (Throwable t) {
						VidLib.LOGGER.error("Failed to load player pin from editor state", t);
					}
				}
			}
		}
	}

	private static void editorStateSaved(JsonObject customData) {
		if (!Pins.PINS.isEmpty()) {
			var pins = new JsonArray();

			for (var pin : Pins.PINS.values()) {
				var pinJson = new JsonObject();
				pinJson.addProperty("uuid", pin.uuid().toString());
				pinJson.addProperty("name", pin.name());
				pinJson.addProperty("enabled", pin.enabled().get());
				pinJson.addProperty("path", pin.path());
				pins.add(pinJson);
			}

			customData.add("vidlib:pins", pins);
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

	private static void popups(ImGraphics graphics) {
		var mc = Minecraft.getInstance();

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
				ClientProps.OPEN_PROPS.add(prop.id);
				ImGui.text(prop.toString());
				prop.imgui(graphics, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
			}

			ImGui.endPopup();
		}

		if (!ImGui.isPopupOpen("###vidlib-prop-popup")) {
			selectedProp = 0;
		}

		int w = mc.getWindow().getWidth();
		int h = mc.getWindow().getHeight();
		int scw = mc.getWindow().getGuiScaledWidth();
		int sch = mc.getWindow().getGuiScaledHeight();

		// Fix flashback lagging extremely after window is minimized
		if (scw > w || sch > h) {
			mc.resizeDisplay();
		}
	}

	private static void icons(CharConsumer chars) {
		for (var icon : ImIcons.VALUES) {
			if (icon.icon != 0) {
				chars.accept(icon.icon);
			}
		}

		for (var c : ImIcons.EXTRA_ICONS.get()) {
			chars.accept(c.toChar());
		}
	}
}
