package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.misc.DebugText;
import dev.beast.mods.shimmer.math.Range;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class PhysicsParticlesTool implements ShimmerTool {
	@AutoInit
	public static void bootstrap() {
		ShimmerTool.register(new PhysicsParticlesTool());
	}

	@Override
	public String getId() {
		return "physics_particles";
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
	public boolean use(Player player, ItemStack item) {
		if (player.level().isClientSide()) {
			explode(player, item);
		}

		return true;
	}

	private void explode(Player player, ItemStack item) {
		var ray = player.ray(400D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);

		if (ray != null) {
			var mc = Minecraft.getInstance();

			for (var pos : BlockPos.betweenClosed(ray.getBlockPos().offset(-4, -4, -4), ray.getBlockPos().offset(4, 1, 4))) {
				var state = player.level().getBlockState(pos);

				if (!state.isAir()) {
					var particles = new PhysicsParticles(player.level().random);
					particles.at = pos;
					particles.state = state;
					particles.tint(mc.getBlockColors().getColor(state, player.level(), pos, 0));
					particles.vvel = Range.of(0F, 1F);
					particles.hvel = Range.of(3F, 4F);
					particles.ttl = Range.of(40F, 200F);
					particles.density = 20F;
					particles.spawn();
				}
			}
		}
	}

	@Override
	public void debugText(Player player, ItemStack item, @Nullable HitResult result, DebugText debugText) {
		int solid = PhysicsParticleManager.SOLID.particles.size();
		int cutout = PhysicsParticleManager.CUTOUT.particles.size();
		int translucent = PhysicsParticleManager.TRANSLUCENT.particles.size();
		int solidRendered = PhysicsParticleManager.SOLID.rendered;
		int cutoutRendered = PhysicsParticleManager.CUTOUT.rendered;
		int translucentRendered = PhysicsParticleManager.TRANSLUCENT.rendered;
		debugText.topLeft.add("%,d/%,d Solid".formatted(solidRendered, solid));
		debugText.topLeft.add("%,d/%,d Cutout".formatted(cutoutRendered, cutout));
		debugText.topLeft.add("%,d/%,d Translucent".formatted(translucentRendered, translucent));
		debugText.topRight.add("%,d/%,d Total".formatted(solidRendered + cutoutRendered + translucentRendered, solid + cutout + translucent));
	}
}
