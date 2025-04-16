package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.explosion.ExplosionData;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.network.chat.Component;

@AutoInit
public interface InternalPlayerData {
	DataType<Boolean> SUSPENDED = DataType.PLAYER.builder("suspended", KnownCodec.BOOL, false)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<Component> NICKNAME = DataType.PLAYER.builder("nickname", KnownCodec.TEXT_COMPONENT, Empty.COMPONENT)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<IconHolder> PLUMBOB = DataType.PLAYER.builder("plumbob", IconHolder.KNOWN_CODEC, IconHolder.EMPTY)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<Clothing> CLOTHING = DataType.PLAYER.builder("clothing", Clothing.KNOWN_CODEC, Clothing.NONE)
		.save()
		.sync()
		.syncToAllClients()
		.build();

	DataType<Boolean> SHOW_ZONES = DataType.PLAYER.builder("show_zones", KnownCodec.BOOL, false)
		.save()
		.sync()
		.build();

	DataType<ZoneRenderType> ZONE_RENDER_TYPE = DataType.PLAYER.builder("zone_render_type", ZoneRenderType.KNOWN_CODEC, ZoneRenderType.NORMAL)
		.save()
		.sync()
		.build();

	DataType<BlockFilter> ZONE_BLOCK_FILTER = DataType.PLAYER.builder("zone_block_filter", BlockFilter.KNOWN_CODEC, BlockFilter.ANY.instance())
		.save()
		.sync()
		.onReceived((player, value) -> player.vl$sessionData().refreshBlockZones())
		.build();

	DataType<Boolean> SHOW_ANCHOR = DataType.PLAYER.builder("show_anchor", KnownCodec.BOOL, false)
		.save()
		.sync()
		.build();

	DataType<Float> FLIGHT_SPEED = DataType.PLAYER.builder("flight_speed", KnownCodec.FLOAT, 1F)
		.save()
		.sync()
		.build();

	DataType<ExplosionData> TEST_EXPLOSION = DataType.PLAYER.builder("test_explosion", ExplosionData.KNOWN_CODEC, ExplosionData.DEFAULT)
		.save()
		.sync()
		.allowClientUpdates()
		.build();

	DataType<PhysicsParticleData> TEST_PARTICLES = DataType.PLAYER.builder("test_physics_particles", PhysicsParticleData.KNOWN_CODEC, PhysicsParticleData.DEFAULT)
		.save()
		.sync()
		.allowClientUpdates()
		.build();

	DataType<Double> TEST_CAMERA_SHAKE = DataType.PLAYER.builder("test_camera_shake", KnownCodec.DOUBLE, 30D)
		.save()
		.sync()
		.allowClientUpdates()
		.build();
}
