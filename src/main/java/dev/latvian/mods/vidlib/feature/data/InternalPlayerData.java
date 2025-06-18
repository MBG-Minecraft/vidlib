package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.explosion.ExplosionData;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import net.minecraft.network.chat.Component;

@AutoInit
public interface InternalPlayerData {
	DataKey<Boolean> SUSPENDED = DataKey.PLAYER.buildDefault("suspended", DataTypes.BOOL, false).syncToAllClients().build();
	DataKey<Component> NICKNAME = DataKey.PLAYER.buildDefault("nickname", DataTypes.TEXT_COMPONENT, Empty.COMPONENT).syncToAllClients().build();
	DataKey<IconHolder> PLUMBOB = DataKey.PLAYER.buildDefault("plumbob", IconHolder.DATA_TYPE, IconHolder.EMPTY).syncToAllClients().build();
	DataKey<Clothing> CLOTHING = DataKey.PLAYER.buildDefault("clothing", Clothing.DATA_TYPE, Clothing.NONE).syncToAllClients().build();
	DataKey<Boolean> SHOW_ZONES = DataKey.PLAYER.createDefaultBoolean("show_zones", false);
	DataKey<ZoneRenderType> ZONE_RENDER_TYPE = DataKey.PLAYER.createDefault("zone_render_type", ZoneRenderType.DATA_TYPE, ZoneRenderType.NORMAL);
	DataKey<BlockFilter> ZONE_BLOCK_FILTER = DataKey.PLAYER.buildDefault("zone_block_filter", BlockFilter.DATA_TYPE, BlockFilter.ANY.instance()).onReceived((player, value) -> player.vl$sessionData().refreshBlockZones()).build();
	DataKey<Boolean> SHOW_ANCHOR = DataKey.PLAYER.createDefaultBoolean("show_anchor", false);
	DataKey<Float> FLIGHT_SPEED = DataKey.PLAYER.createDefaultFloat("flight_speed", 1F);
	DataKey<ExplosionData> TEST_EXPLOSION = DataKey.PLAYER.buildDefault("test_explosion", ExplosionData.DATA_TYPE, ExplosionData.DEFAULT).allowClientUpdates().build();
	DataKey<PhysicsParticleData> TEST_PARTICLES = DataKey.PLAYER.buildDefault("test_physics_particles", PhysicsParticleData.DATA_TYPE, PhysicsParticleData.DEFAULT).allowClientUpdates().build();
	DataKey<Double> TEST_SCREEN_SHAKE = DataKey.PLAYER.buildDefault("test_screen_shake", DataTypes.DOUBLE, 30D).allowClientUpdates().build();
	DataKey<Boolean> SHOW_FPS = DataKey.PLAYER.createDefaultBoolean("show_fps", false);
	DataKey<Boolean> ADMIN_PANEL = DataKey.PLAYER.buildDefault("admin_panel", DataTypes.BOOL, false).allowClientUpdates().build();
}
