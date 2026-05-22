package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.gallery.LowQualityPlayerBodies;
import dev.latvian.mods.vidlib.feature.gallery.PlayerBodies;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

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

	public final ImString search;
	public final ImString name;

	private final ImBoolean ignoreNPCs = new ImBoolean(false);
	private GameProfile profile;

	public GameProfileImBuilder() {
		this.name = new ImString(16);
		this.name.inputData.isResizable = false;
		this.name.inputData.allowedChars = VALID_NAME_CHARS.get();

		this.search = new ImString(16);
		this.search.inputData.isResizable = false;
		this.search.inputData.allowedChars = VALID_NAME_CHARS.get();
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

		appendMainIcon(graphics);
		ImGui.sameLine();

		boolean select = ImGui.button(profile == null ? "Select..." : profile.getName());

		if (profile != null && ImGui.isItemHovered()) {
			graphics.tooltip(profile.getId().toString());
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
			var u = profileSelector(graphics, null);
			update = update.or(u);
			if (u.isFull()) {
				ImGui.closeCurrentPopup();
			}

			ImGui.endPopup();
		}

		return update;
	}

	public ImUpdate profileSelector(ImGraphics graphics, @Nullable Predicate<GameProfile> filter) {
		var update = ImUpdate.NONE;

		if (ImGui.beginListBox("###existing", -1F, 180F)) {
			ImGui.setNextItemWidth(-1F);
			ImGui.inputTextWithHint("###search", "Search...", search);

			if (filter == null || filter.test(PlayerProfile.EMPTY_GAME_PROFILE)) {
				appendIcon(graphics.mc, Util.NIL_UUID);

				ImGui.sameLine();
				if (ImGui.selectable(ImIcons.CLOSE + " None", profile == null)) {
					profile = null;
					update = ImUpdate.FULL;
				}
			}

			for (var p : PlayerProfiles.getAllKnown()) {
				if (ignoreNPCs.get() && (p.profile().getId().version() == 2 || p.profile().getId().equals(Util.NIL_UUID))) {
					continue;
				}

				if (filter == null || filter.test(p.profile())) {
					if (search.isNotEmpty() && !p.profile().getName().contains(search.get())) {
						continue;
					}
					appendIcon(graphics.mc, p.profile().getId());
					ImGui.sameLine();

					if (ImGui.selectable(p.profile().getName(), profile != null && p.profile().getId().equals(profile.getId()))) {
						profile = p.profile();
						update = ImUpdate.FULL;
					}
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
			var nameStr = name.get();
			profile = null;

			if (!nameStr.isEmpty()) {
				var p = PlayerProfiles.get(nameStr);
				if (p.isError()) {
					profile = null;
					VidLib.LOGGER.error("Failed to fetch profile '" + nameStr + "'");
				} else {
					profile = p.profile();
				}
			}

			update = ImUpdate.FULL;
		}

		if (ImGui.collapsingHeader("Options###options")) {
			if (ImGui.checkbox("Ignore NPCs###ignore-npcs", ignoreNPCs)) {
				update = ImUpdate.PARTIAL;
			}
		}

		return update;
	}

	private void appendMainIcon(ImGraphics graphics) {
		var tex = LowQualityPlayerBodies.getTexture(graphics.mc, profile == null ? null : profile.getId());
		ImGui.image(tex.getTexture().vl$getHandle(), ImGui.getFrameHeight(), ImGui.getFrameHeight());

		if (ImGui.isItemHovered() && graphics.beginTooltip()) {
			var texHD = PlayerBodies.getTexture(graphics.mc, profile == null ? null : profile.getId());
			ImGui.image(texHD.getTexture().vl$getHandle(), 128F, 128F);
			graphics.endTooltip();
		}
	}

	private void appendIcon(Minecraft mc, UUID uuid) {
		var tex = LowQualityPlayerBodies.getTexture(mc, uuid);
		ImGui.image(tex.getTexture().vl$getHandle(), ImGui.getFontSize(), ImGui.getFontSize());
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public GameProfile build() {
		return profile == null ? PlayerProfile.EMPTY_GAME_PROFILE : profile;
	}
}
