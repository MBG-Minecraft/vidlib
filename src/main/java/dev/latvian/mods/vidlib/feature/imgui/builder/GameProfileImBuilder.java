package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.gallery.PlayerHeads;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.util.MiscUtils;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class GameProfileImBuilder implements ImBuilder<GameProfile> {
	public static final ImBuilderType<GameProfile> TYPE = GameProfileImBuilder::new;

	private static final Lazy<String> VALID_NAME_CHARS = Lazy.of(() -> {
		int min = 33;
		int max = 126;
		var chars = new char[max - min + 1];

		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (min + i);
		}

		return new String(chars);
	});

	public final ImString name;
	private GameProfile profile;

	public GameProfileImBuilder() {
		this.name = new ImString(16);
		this.name.inputData.isResizable = false;
		this.name.inputData.allowedChars = VALID_NAME_CHARS.get();
		this.profile = null;
	}

	@Override
	public void set(GameProfile v) {
		if (v != null && !v.getName().isEmpty() && !v.getId().equals(Util.NIL_UUID)) {
			name.set(v.getName());
			profile = v;
		} else {
			name.set("");
			profile = null;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		appendMainIcon(graphics.mc);
		ImGui.sameLine();

		boolean select = ImGui.button(profile == null ? "Select..." : profile.getName());

		if (profile != null && ImGui.isItemHovered()) {
			ImGui.setTooltip(profile.getId().toString());
		}

		if (select) {
			ImGui.openPopup("###select-profile");

			if (profile != null) {
				name.set(profile.getName());
			} else {
				name.set("");
			}
		}

		if (ImGui.beginPopup("Select Profile###select-profile", ImGuiWindowFlags.AlwaysAutoResize)) {
			if (ImGui.beginListBox("###existing", -1F, 120F)) {
				var map = new HashMap<UUID, GameProfile>();

				for (var p : graphics.mc.vl$getCachedGameProfiles()) {
					map.put(p.getId(), p);
				}

				if (graphics.mc.getConnection() != null) {
					for (var info : graphics.mc.getConnection().getOnlinePlayers()) {
						map.put(info.getProfile().getId(), info.getProfile());
					}
				}

				var list = new ArrayList<>(map.values());

				if (list.size() >= 2) {
					list.sort(MiscUtils.PROFILE_COMPARATOR);
				}

				appendIcon(graphics.mc, Util.NIL_UUID, "");
				ImGui.sameLine();

				if (ImGui.selectable(ImIcons.CLOSE + " None", profile == null)) {
					profile = null;
					update = ImUpdate.FULL;
					ImGui.closeCurrentPopup();
				}

				for (var p : list) {
					appendIcon(graphics.mc, p.getId(), p.getName());
					ImGui.sameLine();

					if (ImGui.selectable(p.getName(), profile != null && p.getId().equals(profile.getId()))) {
						profile = p;
						update = ImUpdate.FULL;
						ImGui.closeCurrentPopup();
					}
				}

				ImGui.endListBox();
			}

			ImGui.setNextItemWidth(180F);
			ImGui.inputTextWithHint("###fetch", "Username", name);

			boolean edited = ImGui.isItemDeactivatedAfterEdit();

			ImGui.sameLine();
			boolean button = ImGui.button("Fetch###fetch-button");

			if (edited || button) {
				ImGui.closeCurrentPopup();
				var nameStr = name.get();
				profile = null;

				if (!nameStr.isEmpty()) {
					profile = graphics.mc.retrieveGameProfile(nameStr);

					if (profile == Empty.PROFILE) {
						profile = null;
						VidLib.LOGGER.error("Failed to fetch profile '" + nameStr + "'");
					}
				}

				update = ImUpdate.FULL;
			}

			ImGui.endPopup();
		}

		return update;
	}

	private void appendMainIcon(Minecraft mc) {
		var tex = PlayerHeads.getTexture(mc, profile == null ? null : profile.getId(), profile == null ? "" : profile.getName());
		int texId = tex.getTexture().vl$getHandle();
		ImGui.image(texId, ImGui.getFrameHeight(), ImGui.getFrameHeight());

		if (ImGui.isItemHovered()) {
			ImGui.beginTooltip();
			ImGui.image(texId, 128F, 128F);
			ImGui.endTooltip();
		}
	}

	private void appendIcon(Minecraft mc, UUID uuid, String name) {
		var tex = PlayerHeads.getTexture(mc, uuid, name);
		ImGui.image(tex.getTexture().vl$getHandle(), ImGui.getFontSize(), ImGui.getFontSize());
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public GameProfile build() {
		return profile == null ? Empty.PROFILE : profile;
	}
}
