package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.gallery.PlayerSkins;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;

public class NpcSkinImBuilder extends ListImBuilder<PlayerSkin> {
	private final ImInt randomCount = new ImInt(10);

	public NpcSkinImBuilder() {
		super(NpcSkinImBuilderImpl::new);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		if (ImGui.button("Randomly Generate###randomly-generate")) {
			items.clear();
			var skins = new ArrayList<>(PlayerSkins.GALLERY.images.keySet());
			Collections.shuffle(skins);
			skins.stream()
				.limit(randomCount.get())
				.map(uuid -> PlayerProfiles.get(uuid).profile())
				.map(profile -> {
					var builder = new NpcSkinImBuilderImpl();
					builder.gameProfile.set(profile);
					builder.set(builder.build());
					return builder;
				})
				.forEach(items::add);
			super.imgui(graphics);
			return ImUpdate.FULL;
		}
		ImGui.sameLine();
		if (ImGui.button("Clear List###clear-list")) {
			items.clear();
			super.imgui(graphics);
			return ImUpdate.FULL;
		}
		ImGui.text("Random count: ");
		ImGui.sameLine();
		ImGui.inputInt("Random Count###random-count", randomCount);
		return super.imgui(graphics);
	}

	public static class NpcSkinImBuilderImpl implements ImBuilder<PlayerSkin> {
		public static final ImBuilderType<PlayerSkin> TYPE = NpcSkinImBuilderImpl::new;

		public final ImBuilder<ResourceLocation> texture = TextureImBuilder.SKIN.get();
		public final ImBuilder<GameProfile> gameProfile = GameProfileImBuilder.TYPE.get();
		public final BooleanImBuilder slim = new BooleanImBuilder();
		public final ImBoolean useGameProfile = new ImBoolean(true);

		@Override
		public void set(PlayerSkin value) {
			if (value == null) {
				gameProfile.set(null);
				texture.set(null);
				slim.set(false);
			} else {
				texture.set(value.texture());
				slim.set(value.model() == PlayerSkin.Model.SLIM);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			ImGui.checkbox("Use GameProfile###use-game-profile", useGameProfile);
			if (useGameProfile.get()) {
				update = update.or(gameProfile.imguiKey(graphics, "Game Profile", "game-profile"));
			} else {
				update = update.or(texture.imguiKey(graphics, "Texture", "texture"));
				update = update.or(slim.imguiKey(graphics, "Slim", "slim"));
			}
			return update;
		}

		@Override
		public PlayerSkin build() {
			if (useGameProfile.get()) {
				return Minecraft.getInstance().getSkinManager().getInsecureSkin(gameProfile.build());
			}
			return PlayerSkins.of(new SkinTexture(
				texture.build(),
				slim.build()
			));
		}
	}

}
