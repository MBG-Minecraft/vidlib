package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.data.InternalPlayerData;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.misc.ScreenText;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (hit != null) {
			var blocks = new ArrayList<PositionedBlock>();

			for (var pos : BlockPos.betweenClosed(hit.getBlockPos().offset(-4, -1, -4), hit.getBlockPos().offset(4, 1, 4))) {
				var state = player.level().getBlockState(pos);

				if (!state.isAir()) {
					blocks.add(new PositionedBlock(pos.immutable(), state));
				}
			}

			player.level().physicsParticles(player.get(InternalPlayerData.TEST_PARTICLES), blocks);
		}

		return true;
	}

	@Override
	public boolean leftClick(Player player, ItemStack item) {
		player.openItemGui(item, InteractionHand.MAIN_HAND);
		return true;
	}

	@Override
	public void debugText(Player player, ItemStack item, @Nullable HitResult hit, ScreenText screenText) {
		int total = 0;
		int totalRendered = 0;

		for (var manager : PhysicsParticleManager.ALL) {
			total += manager.particles.size();
			totalRendered += manager.rendered;
			screenText.topLeft.add("%,d/%,d %s".formatted(manager.rendered, manager.particles.size(), manager.displayName));
		}

		screenText.topRight.add(Minecraft.getInstance().fpsString.split(" ", 2)[0] + " FPS");
		screenText.topRight.add("%,d/%,d Total".formatted(totalRendered, total));
	}
}
