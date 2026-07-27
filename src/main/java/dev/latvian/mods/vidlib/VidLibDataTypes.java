package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.clothing.ClothingSet;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.decal.DecalType;
import dev.latvian.mods.vidlib.feature.entity.EntitySnapshot;
import dev.latvian.mods.vidlib.feature.explosion.ExplosionData;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverrides;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.stage.Stage;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.util.NameDrawType;
import io.netty.buffer.ByteBuf;

public interface VidLibDataTypes {
	static void register(CustomRegistryTypeCollector<ByteBuf, DataType<?>> registry) {
		registry.register("clothing_set", ClothingSet.DATA_TYPE);
		registry.register("player_clothing", PlayerClothing.DATA_TYPE);
		registry.register("skin_texture", SkinTexture.DATA_TYPE);
		registry.register("skin_texture_list", SkinTexture.LIST_DATA_TYPE);
		registry.register("chanced_particle", ChancedParticle.DATA_TYPE);
		registry.register("chanced_particle_list", ChancedParticle.LIST_DATA_TYPE);
		registry.register("zone_render_type", ZoneRenderType.DATA_TYPE);
		registry.register("anchor", Anchor.DATA_TYPE);
		registry.register("screen_shake", ScreenShake.DATA_TYPE);
		registry.register("prop_type", PropType.DATA_TYPE);
		registry.register("explosion_data", ExplosionData.DATA_TYPE);
		registry.register("positioned_sound_data", PositionedSoundData.DATA_TYPE);
		registry.register("stage", Stage.DATA_TYPE);
		registry.register("name_draw_type", NameDrawType.DATA_TYPE);
		registry.register("entity_snapshot", EntitySnapshot.DATA_TYPE);
		registry.register("entity_snapshot_list", EntitySnapshot.LIST_DATA_TYPE);
		registry.register("waypoint", Waypoint.DATA_TYPE);
		registry.register("waypoint_list", Waypoint.LIST_DATA_TYPE);
		registry.register("player_input", PlayerInput.DATA_TYPE);
		registry.register("map_texture_overrides", MapTextureOverrides.DATA_TYPE);
		registry.register("decal", Decal.DATA_TYPE);
		registry.register("decal_type", DecalType.DATA_TYPE);
	}
}
