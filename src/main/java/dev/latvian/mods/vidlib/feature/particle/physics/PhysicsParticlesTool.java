package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;

public class PhysicsParticlesTool implements VidLibTool, PlayerActionHandler {
	@AutoInit
	public static void bootstrap() {
		VidLibTool.register(new PhysicsParticlesTool());
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
	public ResourceLocation getModel() {
		return ResourceLocation.withDefaultNamespace("blaze_rod");
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
	public void debugText(Player player, ItemStack item, @Nullable HitResult hit, ScreenText screenText) {
		PhysicsParticleManager.debugInfo(screenText.topLeft::add, screenText.topRight::add);
	}

	@Override
	public Set<PlayerActionType> getHandledPlayerActions() {
		return PlayerActionType.SWAP_SET;
	}

	@Override
	public boolean onClientPlayerAction(Player player, PlayerActionType action) {
		if (action == PlayerActionType.SWAP) {
			player.openItemGui(player.getMainHandItem(), InteractionHand.MAIN_HAND);
			return true;
		}

		return false;
	}
}
