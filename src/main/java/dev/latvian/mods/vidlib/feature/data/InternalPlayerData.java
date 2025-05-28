package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import dev.latvian.mods.vidlib.feature.explosion.ExplosionData;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.network.chat.Component;

@AutoInit
public interface InternalPlayerData {
	DataKey<Boolean> SUSPENDED = DataKey.PLAYER.builder("suspended", RegisteredDataType.BOOL, false)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataKey<Component> NICKNAME = DataKey.PLAYER.builder("nickname", RegisteredDataType.TEXT_COMPONENT, Empty.COMPONENT)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataKey<IconHolder> PLUMBOB = DataKey.PLAYER.builder("plumbob", IconHolder.REGISTERED_DATA_TYPE, IconHolder.EMPTY)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataKey<Clothing> CLOTHING = DataKey.PLAYER.builder("clothing", Clothing.REGISTERED_DATA_TYPE, Clothing.NONE)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataKey<Boolean> SHOW_ZONES = DataKey.PLAYER.builder("show_zones", RegisteredDataType.BOOL, false)
		.save()
		.sync()
		.build();

	DataKey<ZoneRenderType> ZONE_RENDER_TYPE = DataKey.PLAYER.builder("zone_render_type", ZoneRenderType.KNOWN_CODEC, ZoneRenderType.NORMAL)
		.save()
		.sync()
		.build();

	DataKey<BlockFilter> ZONE_BLOCK_FILTER = DataKey.PLAYER.builder("zone_block_filter", BlockFilter.REGISTERED_DATA_TYPE, BlockFilter.ANY.instance())
		.save()
		.sync()
		.onReceived((player, value) -> player.vl$sessionData().refreshBlockZones())
		.build();

	DataKey<Boolean> SHOW_ANCHOR = DataKey.PLAYER.builder("show_anchor", RegisteredDataType.BOOL, false)
		.save()
		.sync()
		.build();

	DataKey<Float> FLIGHT_SPEED = DataKey.PLAYER.builder("flight_speed", RegisteredDataType.FLOAT, 1F)
		.save()
		.sync()
		.build();

	DataKey<ExplosionData> TEST_EXPLOSION = DataKey.PLAYER.builder("test_explosion", ExplosionData.REGISTERED_DATA_TYPE, ExplosionData.DEFAULT)
		.save()
		.sync()
		.allowClientUpdates()
		.build();

	DataKey<PhysicsParticleData> TEST_PARTICLES = DataKey.PLAYER.builder("test_physics_particles", PhysicsParticleData.REGISTERED_DATA_TYPE, PhysicsParticleData.DEFAULT)
		.save()
		.sync()
		.allowClientUpdates()
		.build();

	DataKey<Double> TEST_CAMERA_SHAKE = DataKey.PLAYER.builder("test_camera_shake", RegisteredDataType.DOUBLE, 30D)
		.save()
		.sync()
		.allowClientUpdates()
		.build();
}
