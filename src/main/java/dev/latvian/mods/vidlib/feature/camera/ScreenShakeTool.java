package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.particle.ShapeParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScreenShakeTool implements VidLibTool {
	@AutoInit
	public static void bootstrap() {
		VidLibTool.register(new ScreenShakeTool());
	}

	@Override
	public String getId() {
		return "screen_shake";
	}

	@Override
	public Component getName() {
		return Component.literal("Screen Shake");
	}

	@Override
	public ResourceLocation getModel() {
		return ResourceLocation.withDefaultNamespace("ender_eye");
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (hit != null && !player.level().isClientSide) {
			var maxDistance = player.get(InternalPlayerData.TEST_SCREEN_SHAKE);
			var pos = hit.getBlockPos().relative(hit.getDirection());
			player.level().cubeParticles(new ShapeParticleOptions(ScreenShake.DEFAULT.duration(), Color.CYAN, Color.WHITE), List.of(pos));
			player.level().screenShake(ScreenShake.DEFAULT, Vec3.atCenterOf(pos), maxDistance);
		}

		return true;
	}
}
