package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverrides;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import dev.latvian.mods.vidlib.util.NameDrawType;

public interface InternalGlobalData {
	DataKey<GlobalSessionData, PlatformType> PLATFORM = DataKey.GLOBAL.createEnum("platform", PlatformType.DATA_TYPE, PlatformType.OTHER);
	DataKey<GlobalSessionData, Boolean> HIDE_PLUMBOBS = DataKey.GLOBAL.createBoolean("hide_plumbobs", false);
	DataKey<GlobalSessionData, NameDrawType> NAME_DRAW_TYPE = DataKey.GLOBAL.createEnum("name_draw_type", NameDrawType.DATA_TYPE, NameDrawType.VANILLA);
	DataKey<GlobalSessionData, Double> NAME_DRAW_MIN_DIST = DataKey.GLOBAL.createDouble("name_draw_min_dist", 20D, 0D, 500D);
	DataKey<GlobalSessionData, Double> NAME_DRAW_MID_DIST = DataKey.GLOBAL.createDouble("name_draw_mid_dist", 170D, 0D, 500D);
	DataKey<GlobalSessionData, Double> NAME_DRAW_MAX_DIST = DataKey.GLOBAL.createDouble("name_draw_max_dist", 230D, 0D, 500D);
	DataKey<GlobalSessionData, Float> NAME_DRAW_MIN_SIZE = DataKey.GLOBAL.createFloat("name_draw_min_size", 0.5F, 0F, 2F);
	DataKey<GlobalSessionData, Boolean> NAME_MUST_SEE = DataKey.GLOBAL.createBoolean("name_must_see", false);
	DataKey<GlobalSessionData, Long> GLOBAL_STOPWATCH = DataKey.GLOBAL.createDefault("global_stopwatch", DataTypes.VAR_LONG, 0L);
	DataKey<GlobalSessionData, Long> GLOBAL_STOPWATCH_START = DataKey.GLOBAL.createDefault("global_stopwatch_start", DataTypes.VAR_LONG, 0L);
	DataKey<GlobalSessionData, MapTextureOverrides> MAP_TEXTURE_OVERRIDES = DataKey.GLOBAL.createDefault("map_texture_overrides", MapTextureOverrides.DATA_TYPE, MapTextureOverrides.EMPTY);
	DataKey<GlobalSessionData, Integer> MIN_FAKE_LAG = DataKey.GLOBAL.createVarInt("min_fake_lag", 0, 0, 1000);
	DataKey<GlobalSessionData, Integer> MAX_FAKE_LAG = DataKey.GLOBAL.createVarInt("max_fake_lag", 0, 0, 1000);

	@AutoInit
	static void bootstrap() {
	}
}
