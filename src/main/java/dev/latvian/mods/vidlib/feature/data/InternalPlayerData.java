package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.SkinTexture;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;

import java.util.Set;

public interface InternalPlayerData {
	DataKey<Boolean> ONLINE = DataKey.PLAYER.builder("online", DataTypes.BOOL, false).sync().build();
	DataKey<String> NAME = DataKey.PLAYER.createDefault("name", DataTypes.STRING, "");
	DataKey<Set<String>> PLAYER_TAGS = DataKey.PLAYER.createDefault("player_tags", DataTypes.STRING_SET, Set.of());
	DataKey<Boolean> SUSPENDED = DataKey.PLAYER.createDefault("suspended", DataTypes.BOOL, false);
	DataKey<Component> NICKNAME = DataKey.PLAYER.createDefault("nickname", DataTypes.TEXT_COMPONENT, Component.empty());
	DataKey<Ref<Icon>> PLUMBOB = DataKey.PLAYER.createDefault("plumbob", Icon.DATA_TYPE, Icon.EMPTY);
	DataKey<PlayerClothing> CLOTHING = DataKey.PLAYER.createDefault("clothing", PlayerClothing.DATA_TYPE, PlayerClothing.NONE);
	DataKey<SkinTexture> SKIN_OVERRIDE = DataKey.PLAYER.createDefault("skin_override", SkinTexture.DATA_TYPE, null);
	DataKey<ClientAsset.ResourceTexture> CAPE_OVERRIDE = DataKey.PLAYER.createDefault("cape_override", DataTypes.RESOURCE_TEXTURE, null);
	DataKey<ClientAsset.ResourceTexture> ELYTRA_OVERRIDE = DataKey.PLAYER.createDefault("elytra_override", DataTypes.RESOURCE_TEXTURE, null);
	DataKey<Float> FLIGHT_SPEED = DataKey.PLAYER.createFloat("flight_speed", 1F, 0F, 20F);
	DataKey<Color> GLOW_COLOR = DataKey.PLAYER.createDefault("glow_color", Color.DATA_TYPE, null);
	DataKey<Boolean> CAN_FLY = DataKey.PLAYER.createBoolean("can_fly", false);
	DataKey<Boolean> TRANSLUCENT = DataKey.PLAYER.createBoolean("translucent", false);
	DataKey<Float> BRIGHTNESS_OVERRIDE = DataKey.PLAYER.createFloat("brightness_override", 0F, 0F, 1F);

	@AutoInit
	static void bootstrap() {
	}
}
