package dev.latvian.mods.vidlib.feature.camera;

import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.particle.CubeParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CameraShakeTool implements VidLibTool {
	@AutoInit
	public static void bootstrap() {
		VidLibTool.register(new CameraShakeTool());
	}

	@Override
	public String getId() {
		return "camera_shake";
	}

	@Override
	public Component getName() {
		return Component.literal("Camera Shake");
	}

	@Override
	public ItemStack createItem() {
		return new ItemStack(Items.ENDER_EYE);
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (hit != null && !player.level().isClientSide) {
			var maxDistance = player.get(InternalPlayerData.TEST_CAMERA_SHAKE);
			var pos = hit.getBlockPos().relative(hit.getDirection());
			player.level().cubeParticles(new CubeParticleOptions(Color.CYAN, Color.WHITE, CameraShake.DEFAULT.duration()), List.of(pos));
			player.level().shakeCamera(CameraShake.DEFAULT, Vec3.atCenterOf(pos), maxDistance);
		}

		return true;
	}
}
