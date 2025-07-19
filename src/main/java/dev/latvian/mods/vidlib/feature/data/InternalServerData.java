package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.skybox.Skyboxes;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AutoInit
public interface InternalServerData {
	DataKey<ResourceLocation> SKYBOX = DataKey.SERVER.createDefault("skybox", SkyboxData.ID_DATA_TYPE, Skyboxes.DAY_WITH_CELESTIALS);
	DataKey<Boolean> IMMUTABLE_WORLD = DataKey.SERVER.createDefaultBoolean("immutable_world", false);
	DataKey<Anchor> ANCHOR = DataKey.SERVER.buildDefault("anchor", Anchor.DATA_TYPE, Anchor.NONE).onReceived((player, anchor) -> Anchor.client = anchor).build();
	DataKey<Boolean> HIDE_PLUMBOBS = DataKey.SERVER.createDefaultBoolean("hide_plumbobs", false);
	DataKey<List<ChancedParticle>> ENVIRONMENT_EFFECTS = DataKey.SERVER.createDefault("environment_effects", ChancedParticle.LIST_DATA_TYPE, List.of());
	DataKey<NameDrawType> NAME_DRAW_TYPE = DataKey.SERVER.createDefault("name_draw_type", NameDrawType.DATA_TYPE, NameDrawType.VANILLA);
	DataKey<Double> NAME_DRAW_MIN_DIST = DataKey.SERVER.createDefaultDouble("name_draw_min_dist", 20D);
	DataKey<Double> NAME_DRAW_MID_DIST = DataKey.SERVER.createDefaultDouble("name_draw_mid_dist", 170D);
	DataKey<Double> NAME_DRAW_MAX_DIST = DataKey.SERVER.createDefaultDouble("name_draw_max_dist", 230D);
	DataKey<Float> NAME_DRAW_MIN_SIZE = DataKey.SERVER.createDefaultFloat("name_draw_min_size", 0.5F);
	DataKey<Long> GLOBAL_STOPWATCH = DataKey.SERVER.createDefault("global_stopwatch", DataTypes.VAR_LONG, 0L);
	DataKey<Long> GLOBAL_STOPWATCH_START = DataKey.SERVER.createDefault("global_stopwatch_start", DataTypes.VAR_LONG, 0L);
}
