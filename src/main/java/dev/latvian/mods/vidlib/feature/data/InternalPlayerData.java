package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.VidLibDataTypes;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothingImBuilder;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.StringImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.feature.skin.SkinTextureImBuilder;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;

import java.util.Set;

public interface InternalPlayerData {
	DataKey<Boolean> ONLINE = DataKey.PLAYER.builder("online", DataTypes.BOOL, false).sync().build();
	DataKey<String> NAME = DataKey.PLAYER.createDefault("name", DataTypes.STRING, "", StringImBuilder.TYPE);
	DataKey<Set<String>> PLAYER_TAGS = DataKey.PLAYER.createDefault("player_tags", VidLibDataTypes.STRING_SET, Set.of(), null);
	DataKey<Boolean> SUSPENDED = DataKey.PLAYER.createDefault("suspended", DataTypes.BOOL, false, BooleanImBuilder.TYPE);
	DataKey<Component> NICKNAME = DataKey.PLAYER.createDefault("nickname", DataTypes.TEXT_COMPONENT, Component.empty(), TextComponentImBuilder.TYPE);
	DataKey<Ref<Icon>> PLUMBOB = DataKey.PLAYER.createDefault("plumbob", Icon.DATA_TYPE, Icon.EMPTY, null);
	DataKey<PlayerClothing> CLOTHING = DataKey.PLAYER.createDefault("clothing", PlayerClothing.DATA_TYPE, PlayerClothing.NONE, PlayerClothingImBuilder.TYPE);
	DataKey<SkinTexture> SKIN_OVERRIDE = DataKey.PLAYER.createDefault("skin_override", SkinTexture.DATA_TYPE, null, SkinTextureImBuilder.TYPE);
	DataKey<ClientAsset.ResourceTexture> CAPE_OVERRIDE = DataKey.PLAYER.createDefault("cape_override", DataTypes.RESOURCE_TEXTURE, null, TextureImBuilder.GEO);
	DataKey<ClientAsset.ResourceTexture> ELYTRA_OVERRIDE = DataKey.PLAYER.createDefault("elytra_override", DataTypes.RESOURCE_TEXTURE, null, TextureImBuilder.GEO);
	DataKey<Float> FLIGHT_SPEED = DataKey.PLAYER.createFloat("flight_speed", 1F, 0F, 20F);
	DataKey<Color> GLOW_COLOR = DataKey.PLAYER.createDefault("glow_color", Color.DATA_TYPE, null, Color4ImBuilder::new);
	DataKey<Boolean> CAN_FLY = DataKey.PLAYER.createBoolean("can_fly", false);
	DataKey<Boolean> TRANSLUCENT = DataKey.PLAYER.createBoolean("translucent", false);
	DataKey<Float> BRIGHTNESS_OVERRIDE = DataKey.PLAYER.createFloat("brightness_override", -1F);

	@AutoInit
	static void bootstrap() {
	}
}
