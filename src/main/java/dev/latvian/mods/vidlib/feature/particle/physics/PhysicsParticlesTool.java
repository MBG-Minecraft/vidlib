package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.misc.SpawnClientParticlePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum PhysicsParticlesTool implements VidLibTool, PlayerActionHandler {
	@AutoRegister
	INSTANCE;

	@Override
	public String getId() {
		return "physics_particles";
	}

	@Override
	public Component getName() {
		return Component.literal("Physics Particles Tool");
	}

	@Override
	public ResourceLocation getModel() {
		return ResourceLocation.withDefaultNamespace("blaze_rod");
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (hit != null && player.level().isClientSide()) {
			clientRightClick(player, hit);
		}

		return true;
	}

	private void clientRightClick(Player player, BlockHitResult hit) {
		var blocks = new ArrayList<PositionedBlock>();
		int radius = 4;
		int depth = 1;

		for (var pos : BlockPos.betweenClosed(hit.getBlockPos().offset(-radius, -depth, -radius), hit.getBlockPos().offset(radius, depth, radius))) {
			var state = player.level().getBlockState(pos);

			if (!state.isAir()) {
				blocks.add(new PositionedBlock(pos.immutable(), state));
			}
		}

		player.c2s(new TestPhysicsParticlesPayload(VidLibClientOptions.TEST_PHYSICS_PARTICLE_DATA.get(), 0L, blocks));
	}

	@Override
	public void debugText(Player player, ItemStack item, @Nullable HitResult hit, ScreenText screenText) {
		PhysicsParticleManager.debugInfo(screenText.topLeft::add, screenText.topRight::add);
	}

	@Override
	public Set<PlayerActionType> getHandledPlayerActions() {
		return PlayerActionType.SWAP_AND_RELOAD_SET;
	}

	@Override
	public boolean onClientPlayerAction(Player player, ItemStack item, InteractionHand hand, PlayerActionType action) {
		if (action == PlayerActionType.SWAP) {
			player.openItemGui(player.getMainHandItem(), InteractionHand.MAIN_HAND);
			return true;
		}

		return false;
	}

	@Override
	public void onPlayerAction(ServerPlayer player, ItemStack item, InteractionHand hand, PlayerActionType action) {
		if (action == PlayerActionType.RELOAD) {
			player.s2c(new SpawnClientParticlePayload(
				List.of(
					Either.right("vidlib:shape{color:cyan,outline_color:transparent,shape:{type:cube,size:0.2}}"),
					Either.right("vidlib:shape{color:magenta,outline_color:transparent,shape:{type:cube,size:0.2}}"),
					Either.right("vidlib:shape{color:yellow,outline_color:transparent,shape:{type:cube,size:0.2}}")
				),                    // particles
				player.position(),    // pos
				new Vec3(2D, 1D, 2D), // spread
				0,                    // spread type (0 = cubic, 1 = spherical) (not implemented)
				new Vec3(0D, 0D, 0D), // velocity
				new Vec3(1D, 1D, 1D), // min velocity multiplier
				new Vec3(1D, 1D, 1D), // max velocity multiplier
				20                    // count
			));
		}
	}
}
