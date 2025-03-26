package dev.beast.mods.shimmer.feature.explosion;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.misc.ScreenText;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExplosionTestTool implements ShimmerTool {
	public static final ExplosionData DEFAULT_DATA = new ExplosionData();
	public static BlockPos lastClickPos = null;

	@AutoInit
	public static void bootstrap() {
		ShimmerTool.register(new ExplosionTestTool());
	}

	@Override
	public String getId() {
		return "explosion_test";
	}

	@Override
	public Component getName() {
		return Component.literal("Explosion Test Tool");
	}

	@Override
	public ItemStack createItem() {
		return new ItemStack(Items.TNT);
	}

	@Override
	public boolean use(Player player, ItemStack item) {
		if (player.level() instanceof ServerLevel && player.isShiftKeyDown()) {
			player.status("Modified %,d blocks".formatted(player.level().undoLastModification()));
		} else {
			var ray = player.ray(400D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);
			lastClickPos = ray != null ? ray.getBlockPos() : null;

			if (ray != null && player.level() instanceof ServerLevel level) {
				explode(level, player, item, ray.getBlockPos());
			}
		}

		return true;
	}

	@Override
	public boolean useOnBlock(Player player, ItemStack item, UseItemOnBlockEvent event) {
		if (player.level() instanceof ServerLevel && player.isShiftKeyDown()) {
			player.status("Modified %,d blocks".formatted(player.level().undoLastModification()));
		} else {
			lastClickPos = event.getPos();

			if (player.level() instanceof ServerLevel level) {
				explode(level, player, item, event.getPos());
			}
		}

		return true;
	}

	public static ExplosionData getData(ItemStack item, boolean newData) {
		ExplosionData explosionData = null;
		var tag = item.get(DataComponents.CUSTOM_DATA);

		if (tag != null && tag.contains("explosion_data")) {
			explosionData = ExplosionData.CODEC.parse(NbtOps.INSTANCE, tag.getUnsafe().get("explosion_data")).result().orElse(null);
		}

		return explosionData == null ? newData ? new ExplosionData() : DEFAULT_DATA : explosionData;
	}

	public void explode(ServerLevel level, Player player, ItemStack item, BlockPos pos) {
		var instance = getData(item, false).instance(level, pos);
		int count = instance.create();

		if (count > 0) {
			player.status("Modified %,d blocks".formatted(count));
		}

		player.status("Displaying Entity Damage");
		var center = Vec3.atCenterOf(pos);
		var blocks = new ArrayList<List<BlockPos>>();

		for (int i = 0; i < 30; i++) {
			blocks.add(new ArrayList<>());
		}

		for (var bpos : BlockPos.betweenClosed(instance.data.getBounds(center).inflate(0.5D))) {
			var inside = instance.data.inside(
				(float) (bpos.getX() + 0.5D - center.x),
				(float) (bpos.getY() + 0.5D - center.y),
				(float) (bpos.getZ() + 0.5D - center.z)
			);

			if (inside >= 0D && inside <= 1D && level.getBlockState(bpos).isAir() && !level.getBlockState(bpos.below()).isAir()) {
				blocks.get(Math.clamp((int) (instance.data.entity.damageEasing.easeClamped(inside) * (blocks.size() - 1D)), 0, blocks.size() - 1)).add(bpos.immutable());
			}
		}

		var map = new HashMap<CubeParticleOptions, List<BlockPos>>();

		for (int i = 0; i < blocks.size(); i++) {
			map.put(new CubeParticleOptions(Color.hsb(KMath.lerp(i / (float) blocks.size(), 0F, 0.5F), 1F, 1F, 255), Color.TRANSPARENT, -120), blocks.get(i));
		}

		level.spawnCubeParticles(map);
	}

	@Override
	public boolean leftClick(Player player, ItemStack item) {
		player.openItemGui(item, InteractionHand.MAIN_HAND);
		return true;
	}

	@Override
	public void debugText(Player player, ItemStack item, @Nullable HitResult result, ScreenText screenText) {
		var data = getData(item, false);
		data.debugText(screenText.topLeft);

		if (data.entity.maxDamage > 0F) {
			var dist = Math.max(data.radius, Math.max(data.depth, data.height));
			var eye = player.getEyePosition();

			if (lastClickPos != null && lastClickPos.closerToCenterThan(eye, dist)) {
				var pos = eye.subtract(Vec3.atCenterOf(lastClickPos));
				float inside = data.inside((float) pos.x, (float) pos.y, (float) pos.z);

				screenText.topRight.add("Inside: " + KMath.format(inside));
				screenText.topRight.add("Damage: " + KMath.format(data.entity.damage(inside)));
			}
		}
	}
}
