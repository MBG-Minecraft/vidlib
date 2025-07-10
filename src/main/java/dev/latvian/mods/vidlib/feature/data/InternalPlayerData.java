package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import net.minecraft.network.chat.Component;

@AutoInit
public interface InternalPlayerData {
	DataKey<Boolean> SUSPENDED = DataKey.PLAYER.buildDefault("suspended", DataTypes.BOOL, false).syncToAllClients().build();
	DataKey<Component> NICKNAME = DataKey.PLAYER.buildDefault("nickname", DataTypes.TEXT_COMPONENT, Empty.COMPONENT).syncToAllClients().build();
	DataKey<IconHolder> PLUMBOB = DataKey.PLAYER.buildDefault("plumbob", IconHolder.DATA_TYPE, IconHolder.EMPTY).syncToAllClients().build();
	DataKey<Clothing> CLOTHING = DataKey.PLAYER.buildDefault("clothing", Clothing.DATA_TYPE, Clothing.NONE).syncToAllClients().build();
	DataKey<Float> FLIGHT_SPEED = DataKey.PLAYER.createDefaultFloat("flight_speed", 1F);
	DataKey<PhysicsParticleData> TEST_PARTICLES = DataKey.PLAYER.buildDefault("test_physics_particles", PhysicsParticleData.DATA_TYPE, PhysicsParticleData.DEFAULT).allowClientUpdates().build();
	DataKey<Double> TEST_SCREEN_SHAKE = DataKey.PLAYER.buildDefault("test_screen_shake", DataTypes.DOUBLE, 30D).allowClientUpdates().build();
}
