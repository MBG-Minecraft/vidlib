package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.visual.PlayerHeadTexture;
import dev.latvian.mods.vidlib.util.MiscUtils;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public class GameProfileImBuilder implements ImBuilder<GameProfile> {
	public static final ImBuilderSupplier<GameProfile> SUPPLIER = GameProfileImBuilder::new;

	private static final ResourceLocation INVALID_UUID_TEXTURE = VidLib.id("textures/misc/no.png");

	public final ImString name = ImGuiUtils.resizableString();
	private GameProfile profile = null;

	@Override
	public void set(GameProfile v) {
		if (v != null) {
			name.set(v.getName());
			profile = new GameProfile(v.getId(), v.getName());
		} else {
			name.set("");
			profile = null;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		AbstractTexture tex;

		if (profile != null) {
			tex = PlayerHeadTexture.get(profile.getId());
		} else {
			tex = graphics.mc.getTextureManager().getTexture(INVALID_UUID_TEXTURE);
		}

		int texId = tex.getTexture().vl$getHandle();
		ImGui.image(texId, ImGui.getFrameHeight(), ImGui.getFrameHeight());

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
				var list = new ArrayList<GameProfile>();

				for (var info : graphics.mc.getConnection().getOnlinePlayers()) {
					list.add(info.getProfile());
				}

				if (list.size() >= 2) {
					list.sort(MiscUtils.PROFILE_COMPARATOR);
				}

				for (var p : list) {
					if (ImGui.selectable(p.getName(), profile != null && p.getId().equals(profile.getId()))) {
						profile = new GameProfile(p.getId(), p.getName());
						update = ImUpdate.FULL;
						ImGui.closeCurrentPopup();
					}
				}

				ImGui.endListBox();
			}

			ImGui.alignTextToFramePadding();
			ImGui.text("Fetch");
			ImGui.sameLine();
			ImGui.setNextItemWidth(180F);
			ImGui.inputText("###fetch", name);

			boolean edited = ImGui.isItemDeactivatedAfterEdit();

			ImGui.sameLine();
			boolean button = ImGui.button("Save###save");

			if (edited || button) {
				ImGui.closeCurrentPopup();
				var nameStr = name.get();
				profile = null;

				if (!nameStr.isEmpty()) {
					try {
						profile = MiscUtils.fetchProfile(nameStr);
					} catch (Exception ex) {
						VidLib.LOGGER.error("Failed to fetch profile '" + nameStr + "'", ex);
					}
				}

				update = ImUpdate.FULL;
			}

			ImGui.endPopup();
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return profile != null;
	}

	@Override
	public GameProfile build() {
		return profile;
	}
}
