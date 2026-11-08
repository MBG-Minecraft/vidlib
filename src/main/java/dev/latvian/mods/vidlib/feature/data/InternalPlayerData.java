package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.SkinTexture;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;

public interface InternalPlayerData {
	DataKey<PlayerSessionData, Boolean> ONLINE = DataKey.PLAYER.builder("online", DataTypes.BOOL, false).sync().build();
	DataKey<PlayerSessionData, Boolean> STAFF = DataKey.PLAYER.createBoolean("staff", false);
	DataKey<PlayerSessionData, Boolean> TALENT = DataKey.PLAYER.createBoolean("talent", false);
	DataKey<PlayerSessionData, Boolean> CAN_FLY = DataKey.PLAYER.createBoolean("can_fly", false);
	DataKey<PlayerSessionData, String> NAME = DataKey.PLAYER.createDefault("name", DataTypes.STRING, "");
	DataKey<PlayerSessionData, Boolean> SUSPENDED = DataKey.PLAYER.createDefault("suspended", DataTypes.BOOL, false);
	DataKey<PlayerSessionData, Component> NICKNAME = DataKey.PLAYER.createDefault("nickname", DataTypes.TEXT_COMPONENT, Component.empty());
	DataKey<PlayerSessionData, Ref<Icon>> PLUMBOB = DataKey.PLAYER.createDefault("plumbob", Icon.DATA_TYPE, Icon.EMPTY);
	DataKey<PlayerSessionData, PlayerClothing> CLOTHING = DataKey.PLAYER.createDefault("clothing", PlayerClothing.DATA_TYPE, PlayerClothing.NONE);
	DataKey<PlayerSessionData, SkinTexture> SKIN_OVERRIDE = DataKey.PLAYER.createDefault("skin_override", SkinTexture.DATA_TYPE, null);
	DataKey<PlayerSessionData, ClientAsset.Texture> CAPE_OVERRIDE = DataKey.PLAYER.createDefault("cape_override", DataTypes.TEXTURE, null);
	DataKey<PlayerSessionData, ClientAsset.Texture> ELYTRA_OVERRIDE = DataKey.PLAYER.createDefault("elytra_override", DataTypes.TEXTURE, null);
	DataKey<PlayerSessionData, Float> FLIGHT_SPEED_MOD = DataKey.PLAYER.createFloat("flight_speed_mod", 1F, 0F, 20F);
	DataKey<PlayerSessionData, Color> GLOW_COLOR = DataKey.PLAYER.createDefault("glow_color", Color.DATA_TYPE, null);
	DataKey<PlayerSessionData, Boolean> TRANSLUCENT = DataKey.PLAYER.createBoolean("translucent", false);
	DataKey<PlayerSessionData, Float> BRIGHTNESS_OVERRIDE = DataKey.PLAYER.createFloat("brightness_override", 0F, 0F, 1F);
	DataKey<PlayerSessionData, Boolean> HARDCORE_HEARTS = DataKey.PLAYER.createBoolean("hardcore_hearts", false);

	@AutoInit
	static void bootstrap() {
	}
}
