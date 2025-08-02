package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum ScreenShakeTool implements VidLibTool {
	@AutoRegister
	INSTANCE;

	@Override
	public String getId() {
		return "screen_shake";
	}

	@Override
	public Component getName() {
		return Component.literal("Screen Shake Tool");
	}

	@Override
	public ResourceLocation getModel() {
		return ResourceLocation.withDefaultNamespace("ender_eye");
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (hit != null && player.level().isClientSide()) {
			clientRightClick(player, hit);
		}

		return true;
	}

	private void clientRightClick(Player player, BlockHitResult hit) {
		var maxDistance = VidLibClientOptions.TEST_SCREEN_SHAKE_MAX_DISTANCE.get();
		var pos = hit.getBlockPos().relative(hit.getDirection());
		player.c2s(new TestScreenShakePayload(ScreenShake.DEFAULT, Optional.of(Vec3.atCenterOf(pos)), maxDistance));
	}
}
