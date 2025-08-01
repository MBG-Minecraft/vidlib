package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.block.BlockStatePalette;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.clothing.ClothingParts;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.explosion.ExplosionData;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.skybox.FogOverride;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.stage.Stage;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.util.NameDrawType;
import dev.latvian.mods.vidlib.util.ScreenCorner;

public class VidLibDataTypes {
	public static void register() {
		DataType.register(VidLib.id("icon"), Icon.DATA_TYPE);
		DataType.register(VidLib.id("icon_holder"), IconHolder.DATA_TYPE);
		DataType.register(VidLib.id("clothing_parts"), ClothingParts.DATA_TYPE);
		DataType.register(VidLib.id("clothing"), Clothing.DATA_TYPE);
		DataType.register(VidLib.id("skybox_id"), SkyboxData.ID_DATA_TYPE);
		DataType.register(VidLib.id("fog_override"), FogOverride.DATA_TYPE);
		DataType.register(VidLib.id("chanced_particle"), ChancedParticle.DATA_TYPE);
		DataType.register(VidLib.id("chanced_particle_list"), ChancedParticle.LIST_DATA_TYPE);
		DataType.register(VidLib.id("block_filter"), BlockFilter.DATA_TYPE);
		DataType.register(VidLib.id("entity_filter"), EntityFilter.DATA_TYPE);
		DataType.register(VidLib.id("zone_render_type"), ZoneRenderType.DATA_TYPE);
		DataType.register(VidLib.id("anchor"), Anchor.DATA_TYPE);
		DataType.register(VidLib.id("direct_cutscene"), Cutscene.DIRECT_DATA_TYPE);
		DataType.register(VidLib.id("cutscene"), Cutscene.DATA_TYPE, Cutscene.REGISTRY, null);
		DataType.register(VidLib.id("screen_shake"), ScreenShake.DATA_TYPE);
		DataType.register(VidLib.id("physics_particle_data"), PhysicsParticleData.DATA_TYPE);
		DataType.register(VidLib.id("positioned_block"), PositionedBlock.DATA_TYPE);
		DataType.register(VidLib.id("positioned_block_list"), PositionedBlock.LIST_DATA_TYPE);
		DataType.register(VidLib.id("prop_type"), PropType.DATA_TYPE);
		DataType.register(VidLib.id("explosion_data"), ExplosionData.DATA_TYPE);
		DataType.register(VidLib.id("screen_corner"), ScreenCorner.DATA_TYPE);
		DataType.register(VidLib.id("clock_font_ref"), ClockFont.REF_DATA_TYPE);
		DataType.register(VidLib.id("zone_container"), ZoneContainer.DATA_TYPE, ZoneContainer.REGISTRY, null);
		DataType.register(VidLib.id("location"), Location.DATA_TYPE, Location.REGISTRY, null);
		DataType.register(VidLib.id("positioned_sound_data"), PositionedSoundData.DATA_TYPE);
		DataType.register(VidLib.id("knumber"), KNumber.DATA_TYPE);
		DataType.register(VidLib.id("kvector"), KVector.DATA_TYPE);
		DataType.register(VidLib.id("stage"), Stage.DATA_TYPE);
		DataType.register(VidLib.id("name_draw_type"), NameDrawType.DATA_TYPE);
		DataType.register(VidLib.id("block_state_palette"), BlockStatePalette.DATA_TYPE);

		// RegisteredDataType<ZoneContainer> REGISTERED_DATA_TYPE = RegisteredDataType.of(REGISTRY, ZoneContainer.class);
	}
}
