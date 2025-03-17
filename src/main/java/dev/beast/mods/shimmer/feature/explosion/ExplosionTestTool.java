package dev.beast.mods.shimmer.feature.explosion;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.misc.DebugText;
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
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

public class ExplosionTestTool implements ShimmerTool {
	public static final ExplosionData DEFAULT_DATA = new ExplosionData();

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
		if (player.level() instanceof ServerLevel level) {
			explode(level, player, item);
		}

		return true;
	}

	@Override
	public boolean useOnBlock(Player player, ItemStack item, UseItemOnBlockEvent event) {
		if (player.level() instanceof ServerLevel level) {
			explode(level, player, item);
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

	public void explode(ServerLevel level, Player player, ItemStack item) {
		var ray = player.ray(400D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);

		if (ray != null) {
			var instance = getData(item, false).instance(level, ray.getBlockPos());
			int count = instance.create();
			level.addUndoable(instance.createUndoableModification());
			player.status("Modified %,d blocks".formatted(count));
		}
	}

	@Override
	public boolean leftClick(Player player, ItemStack item) {
		player.openItemGui(item, InteractionHand.MAIN_HAND);
		return true;
	}

	@Override
	public void debugText(Player player, ItemStack item, @Nullable HitResult result, DebugText debugText) {
		getData(item, false).debugText(debugText.topLeft);
	}
}
