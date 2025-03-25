package dev.beast.mods.shimmer.feature.camerashake;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.data.InternalPlayerData;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.math.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

public class CameraShakeTool implements ShimmerTool {
	@AutoInit
	public static void bootstrap() {
		ShimmerTool.register(new CameraShakeTool());
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
	public boolean use(Player player, ItemStack item) {
		var ray = player.ray(400D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);

		if (ray != null) {
			var maxDistance = player.get(InternalPlayerData.TEST_CAMERA_SHAKE);
			var pos = ray.getBlockPos().relative(ray.getDirection());

			if (player.level().isClientSide) {
				player.level().addParticle(new CubeParticleOptions(Color.CYAN, Color.WHITE, CameraShake.DEFAULT.duration()), true, true, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
			} else {
				for (var to : player.level().players()) {
					to.shakeCamera(CameraShake.DEFAULT.atDistance(to.position(), Vec3.atCenterOf(pos), maxDistance));
				}
			}
		}

		return true;
	}

	private void shake(Player player) {

	}
}
