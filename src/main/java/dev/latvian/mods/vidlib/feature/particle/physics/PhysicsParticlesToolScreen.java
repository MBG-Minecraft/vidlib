package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.config.ConfigScreen;
import dev.latvian.mods.vidlib.feature.item.ItemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PhysicsParticlesToolScreen extends ConfigScreen<PhysicsParticleData> {
	@AutoInit(AutoInit.Type.CLIENT_LOADED)
	public static void registerScreen() {
		ItemScreen.TOOLS.put("physics_particles", PhysicsParticlesToolScreen::new);
	}

	private PhysicsParticlesToolScreen(Player player, ItemStack stack, InteractionHand hand) {
		super(
			player.level().registryAccess().createSerializationContext(JsonOps.INSTANCE),
			VidLibClientOptions.TEST_PHYSICS_PARTICLE_DATA.get(),
			PhysicsParticleData.DEFAULT,
			PhysicsParticleData.CONFIG,
			data -> {
				VidLibClientOptions.TEST_PHYSICS_PARTICLE_DATA.set(data);
				Minecraft.getInstance().options.save();
			}
		);
	}

	@Override
	protected void init() {
		super.init();
		addCopyJsonButton(PhysicsParticleData.CODEC);
	}
}
