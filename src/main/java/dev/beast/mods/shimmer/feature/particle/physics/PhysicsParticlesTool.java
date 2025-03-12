package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.toolitem.ShimmerTool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class PhysicsParticlesTool implements ShimmerTool {
	@AutoInit
	public static void bootstrap() {
		ShimmerTool.REGISTRY.put("physics_particles", new PhysicsParticlesTool());
	}

	@Override
	public Component getName() {
		return Component.literal("Physics Particles");
	}

	@Override
	public ItemStack createItem() {
		return new ItemStack(Items.BLAZE_ROD);
	}

	@Override
	public boolean use(Player player, PlayerInteractEvent.RightClickItem event) {
		if (event.getLevel().isClientSide()) {
			explode(player);
		}

		return true;
	}

	private void explode(Player player) {
		var ray = player.ray(200D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);

		if (ray != null) {
			var mc = Minecraft.getInstance();
			var pos = ray.getBlockPos();
			var state = player.level().getBlockState(pos);

			var particles = new PhysicsParticles(player.level().random);
			particles.at = pos;
			particles.state = state;
			particles.tint(mc.getBlockColors().getColor(state, player.level(), pos, 0));
			particles.spawn();
		}
	}
}
