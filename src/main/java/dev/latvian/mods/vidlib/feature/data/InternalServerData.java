package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.skybox.Skyboxes;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.util.NameRenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AutoInit
public interface InternalServerData {
	DataKey<ResourceLocation> SKYBOX = DataKey.SERVER.createDefault("skybox", SkyboxData.ID_DATA_TYPE, Skyboxes.DAY_WITH_CELESTIALS);
	DataKey<Boolean> IMMUTABLE_WORLD = DataKey.SERVER.createDefaultBoolean("immutable_world", false);
	DataKey<Anchor> ANCHOR = DataKey.SERVER.buildDefault("anchor", Anchor.DATA_TYPE, Anchor.NONE).onReceived((player, anchor) -> Anchor.client = anchor).build();
	DataKey<Boolean> HIDE_PLUMBOBS = DataKey.SERVER.createDefaultBoolean("hide_plumbobs", false);
	DataKey<List<ChancedParticle>> ENVIRONMENT_EFFECTS = DataKey.SERVER.createDefault("environment_effects", ChancedParticle.LIST_DATA_TYPE, List.of());
	DataKey<NameRenderType> NAME_RENDER_TYPE = DataKey.SERVER.createDefault("name_render_type", NameRenderType.DATA_TYPE, NameRenderType.DEFAULT);
	DataKey<Long> GLOBAL_STOPWATCH = DataKey.SERVER.createDefault("name_render_type", DataTypes.VAR_LONG, 0L);
	DataKey<Long> GLOBAL_STOPWATCH_START = DataKey.SERVER.createDefault("name_render_type", DataTypes.VAR_LONG, 0L);
}
