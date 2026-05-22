package dev.latvian.mods.vidlib.feature.environment;

import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.builder.SpriteKeyImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverride;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverrides;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverridesReplaySessionData;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import imgui.ImGui;
import net.minecraft.nbt.TagParser;

public class MapTextureOverridePanel extends Panel {
	public static final MapTextureOverridePanel INSTANCE = new MapTextureOverridePanel();

	public static final MenuItem MENU_ITEM = MenuItem.item(ImIcons.TEXT_DOCUMENT, "Map Texture Overrides", INSTANCE);

	public MapTextureOverridePanel() {
		super("map-texture-overrides", "Map Texture Overrides");
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			return;
		}

		ImGui.pushItemWidth(-1F);

		MapTextureOverrides map;

		var replayData = ReplayAPI.getActiveSessionData(MapTextureOverridesReplaySessionData.TYPE);

		if (replayData != null) {
			map = replayData.overrides;
		} else {
			map = graphics.session.serverDataMap.get(InternalServerData.MAP_TEXTURE_OVERRIDES);
		}

		int remove = -1;

		if (ImGui.button(ImIcons.COPY + " Copy###copy-all")) {
			try {
				ImGui.setClipboardText(MapTextureOverrides.CODEC.encodeStart(graphics.nbtOps, map).getOrThrow().toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (ImGui.isItemHovered()) {
			try {
				graphics.tooltip(MapTextureOverrides.CODEC.encodeStart(graphics.nbtOps, map).getOrThrow().toString());
			} catch (Exception ignore) {
			}
		}

		ImGui.sameLine();

		if (ImGui.button(ImIcons.PASTE + " Paste###paste-all")) {
			try {
				var newMap = MapTextureOverrides.CODEC.parse(graphics.nbtOps, TagParser.create(graphics.nbtOps).parseFully(ImGui.getClipboardText())).getOrThrow();
				map.list.clear();
				map.join(newMap);
				remove = -2;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		ImGui.separator();

		for (int i = 0; i < map.list.size(); i++) {
			var entry = map.list.get(i);
			var update = ImUpdate.NONE;

			ImGui.pushID(i);

			if (graphics.button("Delete###delete", ImColorVariant.RED)) {
				remove = i;
			}

			ImGui.sameLine();

			ImGuiUtils.INT.set(entry.mapId());
			ImGui.inputInt("###id", ImGuiUtils.INT);
			int mapId = ImGuiUtils.INT.get();
			update = update.orItemEdit();

			var builder = SpriteKeyImBuilder.TYPE.get();
			builder.set(entry.sprite());

			update = update.or(builder.imguiKey(graphics, "", "sprite"));

			if (update.isAny() && builder.isValid()) {
				map.list.set(i, new MapTextureOverride(mapId, builder.build()));
				remove = -2;
			}

			ImGui.popID();

			ImGui.separator();
		}

		if (graphics.smallButton("Add###add", ImColorVariant.GREEN)) {
			map.list.add(new MapTextureOverride(0, SpriteKey.special(VidLibTextures.LOGO)));
			remove = -2;
		}

		if (remove != -1) {
			if (remove >= 0) {
				map.list.remove(remove);
			}

			map.update();

			if (replayData == null) {
				graphics.mc.updateServerDataValue(InternalServerData.MAP_TEXTURE_OVERRIDES, map);
			}
		}

		ImGui.popItemWidth();
	}
}
