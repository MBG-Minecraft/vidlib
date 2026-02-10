package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.skybox.Skyboxes;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface InternalServerData {
	DataKey<ResourceLocation> SKYBOX = DataKey.SERVER.createDefault("skybox", SkyboxData.ID_DATA_TYPE, Skyboxes.DAY_WITH_CELESTIALS, () -> new EnumImBuilder<>(SkyboxData.SKYBOX_IDS, Skyboxes.DAY_WITH_CELESTIALS).withNameGetter(ID::idToString));
	DataKey<Anchor> ANCHOR = DataKey.SERVER.createDefault("anchor", Anchor.DATA_TYPE, Anchor.NONE, null);
	DataKey<Boolean> HIDE_PLUMBOBS = DataKey.SERVER.createBoolean("hide_plumbobs", false);
	DataKey<NameDrawType> NAME_DRAW_TYPE = DataKey.SERVER.createEnum("name_draw_type", NameDrawType.DATA_TYPE, NameDrawType.VANILLA, NameDrawType.VALUES);
	DataKey<Double> NAME_DRAW_MIN_DIST = DataKey.SERVER.createDouble("name_draw_min_dist", 20D, 0D, 500D);
	DataKey<Double> NAME_DRAW_MID_DIST = DataKey.SERVER.createDouble("name_draw_mid_dist", 170D, 0D, 500D);
	DataKey<Double> NAME_DRAW_MAX_DIST = DataKey.SERVER.createDouble("name_draw_max_dist", 230D, 0D, 500D);
	DataKey<Float> NAME_DRAW_MIN_SIZE = DataKey.SERVER.createFloat("name_draw_min_size", 0.5F, 0F, 2F);
	DataKey<Long> GLOBAL_STOPWATCH = DataKey.SERVER.createDefault("global_stopwatch", DataTypes.VAR_LONG, 0L, null);
	DataKey<Long> GLOBAL_STOPWATCH_START = DataKey.SERVER.createDefault("global_stopwatch_start", DataTypes.VAR_LONG, 0L, null);
	DataKey<Boolean> ICE_MELTS = DataKey.SERVER.createBoolean("ice_melts", false);
	DataKey<Boolean> BLOCK_GRAVITY = DataKey.SERVER.createBoolean("block_gravity", false);
	DataKey<List<Waypoint>> WAYPOINTS = DataKey.SERVER.createDefault("waypoints", Waypoint.LIST_DATA_TYPE, List.of(), null);

	@AutoInit
	static void bootstrap() {
	}
}
