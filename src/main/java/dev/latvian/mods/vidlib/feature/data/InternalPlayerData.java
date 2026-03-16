package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLibDataTypes;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.clothing.ClothingImBuilder;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.StringImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.feature.skin.SkinTextureImBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public interface InternalPlayerData {
	DataKey<Boolean> ONLINE = DataKey.PLAYER.builder("online", DataTypes.BOOL, false).sync().build();
	DataKey<String> NAME = DataKey.PLAYER.createDefault("name", DataTypes.STRING, "", StringImBuilder.TYPE);
	DataKey<Set<String>> PLAYER_TAGS = DataKey.PLAYER.createDefault("player_tags", VidLibDataTypes.STRING_SET, Set.of(), null);
	DataKey<Boolean> SUSPENDED = DataKey.PLAYER.createDefault("suspended", DataTypes.BOOL, false, BooleanImBuilder.TYPE);
	DataKey<Component> NICKNAME = DataKey.PLAYER.createDefault("nickname", DataTypes.TEXT_COMPONENT, Component.empty(), TextComponentImBuilder.TYPE);
	DataKey<IconHolder> PLUMBOB = DataKey.PLAYER.createDefault("plumbob", IconHolder.DATA_TYPE, IconHolder.EMPTY, null);
	DataKey<List<Clothing>> CLOTHING = DataKey.PLAYER.createDefault("clothing", Clothing.LEGACY_CLOTHING_DATA ? Clothing.LEGACY_LIST_DATA_TYPE : Clothing.LIST_DATA_TYPE, List.of(), () -> new ListImBuilder<>(ClothingImBuilder.TYPE));
	DataKey<SkinTexture> SKIN_OVERRIDE = DataKey.PLAYER.createDefault("skin_override", SkinTexture.DATA_TYPE, null, SkinTextureImBuilder.TYPE);
	DataKey<ResourceLocation> CAPE_OVERRIDE = DataKey.PLAYER.createDefault("cape_override", ID.DATA_TYPE, null, TextureImBuilder.GEO);
	DataKey<ResourceLocation> ELYTRA_OVERRIDE = DataKey.PLAYER.createDefault("elytra_override", ID.DATA_TYPE, null, TextureImBuilder.GEO);
	DataKey<Float> FLIGHT_SPEED = DataKey.PLAYER.createFloat("flight_speed", 1F, 0F, 20F);
	DataKey<Color> GLOW_COLOR = DataKey.PLAYER.createDefault("glow_color", Color.DATA_TYPE, null, Color4ImBuilder::new);
	DataKey<Boolean> CAN_FLY = DataKey.PLAYER.createBoolean("can_fly", false);

	@AutoInit
	static void bootstrap() {
	}
}
