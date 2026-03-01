package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class NpcSkinImBuilder implements ImBuilder<PlayerSkin> {
	public static final ImBuilderType<PlayerSkin> TYPE = NpcSkinImBuilder::new;

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
		if (ImGui.checkbox("Use GameProfile", useGameProfile)) {
			update = ImUpdate.FULL;
		}
		if (useGameProfile.get()) {
			update = update.or(gameProfile.imgui(graphics));
		} else {
			update = update.or(texture.imgui(graphics));
			update = update.or(slim.imgui(graphics));
		}
		return update;
	}

	@Override
	public PlayerSkin build() {
		/*if (useGameProfile.get()) {
			return Minecraft.getInstance().getSkinManager().getInsecureSkin(gameProfile.build());
		}*/
		/*return PlayerSkins.of(new SkinTexture(
			texture.build(),
			slim.build()
		));*/
		return Minecraft.getInstance().getSkinManager().getInsecureSkin(gameProfile.build());
	}
}
