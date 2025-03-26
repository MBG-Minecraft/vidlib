package dev.beast.mods.shimmer.feature.bulk;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.item.ShimmerTool;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.registry.RegistryRef;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class BulkModificationToolItem implements ShimmerTool {
	private static final RegistryRef<Lazy<StructureTemplate>> SHIP = StructureStorage.SERVER.ref(ResourceLocation.parse("video:ship"));

	@AutoInit
	public static void bootstrap() {
		ShimmerTool.register(new BulkModificationToolItem());
	}

	@Override
	public String getId() {
		return "bulk";
	}

	@Override
	public Component getName() {
		return Component.literal("Bulk Modification Test Tool");
	}

	@Override
	public ItemStack createItem() {
		return new ItemStack(Items.ECHO_SHARD);
	}

	@Override
	public boolean use(Player player, ItemStack item) {
		if (player.level() instanceof ServerLevel level) {
			var ray = player.ray(400D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);

			if (ray != null) {
				var pos = ray.getBlockPos();
				var state = level.getBlockState(pos);
				level.bulkModify(modifications -> modify(level, modifications, pos, state));
			}
		}

		return true;
	}

	private void modify(ServerLevel level, BlockModificationConsumer modifications, BlockPos pos, BlockState state) {
		modifications.fill(pos.offset(-5, -5, -5), pos.offset(5, 5, 5), Blocks.AIR);
		modifications.set(pos, state);
		modifications.add(BulkLevelModification.structure(
			SHIP,
			pos.offset(0, 3, 0),
			new BlockPos(-4, 0, -14),
			Mirror.NONE,
			Rotation.NONE,
			BlockPos.ZERO,
			0L
		));
	}
}
