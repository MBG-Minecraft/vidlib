package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.clothing.ClothingImBuilder;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import net.minecraft.network.chat.Component;

@AutoInit
public interface InternalPlayerData {
	DataKey<Boolean> SUSPENDED = DataKey.PLAYER.createDefault("suspended", DataTypes.BOOL, false, BooleanImBuilder.TYPE);
	DataKey<Component> NICKNAME = DataKey.PLAYER.createDefault("nickname", DataTypes.TEXT_COMPONENT, Component.empty(), TextComponentImBuilder.TYPE);
	DataKey<IconHolder> PLUMBOB = DataKey.PLAYER.createDefault("plumbob", IconHolder.DATA_TYPE, IconHolder.EMPTY, null);
	DataKey<Clothing> CLOTHING = DataKey.PLAYER.createDefault("clothing", Clothing.DATA_TYPE, Clothing.NONE, ClothingImBuilder.TYPE);
	DataKey<Float> FLIGHT_SPEED = DataKey.PLAYER.createFloat("flight_speed", 1F, 0F, 20F);

	@AutoInit
	static void bootstrap() {
		if (VidLibConfig.legacyDataKeyStream) {
			DataKey.PLAYER.createDefault("test_physics_particles", PhysicsParticleData.DATA_TYPE, PhysicsParticleData.DEFAULT, null);
			DataKey.PLAYER.createDouble("test_screen_shake", 30D);
		}
	}
}
