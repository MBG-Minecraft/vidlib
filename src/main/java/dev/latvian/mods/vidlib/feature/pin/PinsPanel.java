package dev.latvian.mods.vidlib.feature.pin;

import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.Panel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PinsPanel extends Panel {
	public static final PinsPanel INSTANCE = new PinsPanel();

	public PinsPanel() {
		super("pins", "Pins");
		rememberOpen = true;
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			ImGui.text("Not in game");
			return;
		}

		ImGui.checkbox("Enabled###pins-enabled", Pins.ENABLED);
		ImGui.sameLine();

		if (ImGui.button("Show All###pins-show-all")) {
			for (var pin : Pins.PINS.values()) {
				pin.enabled = true;
			}
		}

		ImGui.sameLine();

		if (ImGui.button("Hide All###pins-hide-all")) {
			for (var pin : Pins.PINS.values()) {
				pin.enabled = false;
			}
		}

		ImGui.separator();
		ImGui.text("Pinned Players (" + Pins.PINS.size() + ")");

		if (Pins.PINS.isEmpty()) {
			ImGui.textDisabled("No pins added. Use the entity selector popup to add pins.");
			return;
		}

		var toRemove = new ArrayList<UUID>();

		for (var entry : Pins.PINS.entrySet()) {
			var uuid = entry.getKey();
			var pin = entry.getValue();

			ImGui.pushID(uuid.toString());

			if (ImGui.smallButton(ImIcons.TRASHCAN + "###pin-remove")) {
				toRemove.add(uuid);
			}

			if (ImGui.isItemHovered()) {
				ImGui.setTooltip("Remove pin");
			}

			ImGui.sameLine();

			ImGui.pushStyleColor(ImGuiCol.Text, nameColor(graphics, uuid));
			boolean settingsOpen = ImGui.collapsingHeader(resolveName(graphics, uuid) + "###pin-settings");
			ImGui.popStyleColor();

			if (settingsOpen) {
				var entity = graphics.mc.level == null ? null : graphics.mc.level.getEntity(uuid);
				Pins.settings(graphics, uuid, pin, entity);
			}

			ImGui.popID();
		}

		for (var uuid : toRemove) {
			Pins.PINS.remove(uuid);
			Pins.LAST_KNOWN_POSITIONS.remove(uuid);
		}
	}

	private static int nameColor(ImGraphics graphics, UUID uuid) {
		int rgb = resolveNameColor(graphics, uuid);
		return 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
	}

	private static String resolveName(ImGraphics graphics, UUID uuid) {
		var level = graphics.mc.level;

		if (level != null) {
			var player = level.getPlayerByUUID(uuid);
			if (player != null) {
				return player.getGameProfile().getName();
			}

			var entity = level.getEntity(uuid);
			if (entity != null) {
				return entity.getName().getString();
			}
		}

		return uuid.toString();
	}

	private static int resolveNameColor(ImGraphics graphics, UUID uuid) {
		var level = graphics.mc.level;

		if (level != null) {
			var entity = level.getEntity(uuid);

			if (entity != null) {
				var textColor = entity.getDisplayName().getStyle().getColor();

				if (textColor != null) {
					return textColor.getValue();
				}

				if (entity instanceof Player player) {
					var nickname = player.get(InternalPlayerData.NICKNAME);
					if (nickname != null && nickname.getStyle().getColor() != null) {
						return nickname.getStyle().getColor().getValue();
					}
				}
			}
		}

		return 0xFFFFFF;
	}
}
