package dev.latvian.mods.vidlib.feature.explosion;

import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ExplosionTestTool implements VidLibTool, PlayerActionHandler {
	private static final Set<PlayerActionType> ACTIONS = Set.of(PlayerActionType.RELOAD);
	public static final ExplosionData DEFAULT_DATA = new ExplosionData();
	public static BlockPos lastClickPos = null;

	@AutoInit
	public static void bootstrap() {
		VidLibTool.register(new ExplosionTestTool());
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

	public static ExplosionData getData(ItemStack item, boolean newData) {
		ExplosionData explosionData = null;
		var tag = item.get(DataComponents.CUSTOM_DATA);

		if (tag != null && tag.contains("explosion_data")) {
			explosionData = ExplosionData.CODEC.parse(NbtOps.INSTANCE, tag.getUnsafe().get("explosion_data")).result().orElse(null);
		}

		return explosionData == null ? newData ? new ExplosionData() : DEFAULT_DATA : explosionData;
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		lastClickPos = hit != null ? hit.getBlockPos() : null;

		if (hit != null && player.level() instanceof ServerLevel level) {
			var instance = getData(item, false).instance(level, hit.getBlockPos());

			if (instance.data.destroy > 0F) {
				player.status("Modified %,d blocks".formatted(instance.create()));
			} else {
				player.status("Displaying Entity Damage");
				level.removeAllParticles();
				instance.displayEntityDamage(120);
			}
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

	@Override
	public Set<PlayerActionType> getHandledPlayerActions() {
		return ACTIONS;
	}

	@Override
	public void onPlayerAction(ServerPlayer player, PlayerActionType action) {
		if (action == PlayerActionType.RELOAD) {
			player.status("Modified %,d blocks".formatted(player.level().undoLastModification()));
		}
	}
}
