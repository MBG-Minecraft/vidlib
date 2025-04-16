package dev.latvian.mods.vidlib.feature.bulk;

import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.block.ConnectedBlock;
import dev.latvian.mods.vidlib.feature.block.filter.BlockIdFilter;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.particle.CubeParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.TextParticleOptions;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.feature.structure.LazyStructures;
import dev.latvian.mods.vidlib.feature.structure.StructureStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BulkModificationToolItem implements VidLibTool {
	private static final RegistryRef<LazyStructures> SHIP = StructureStorage.SERVER.ref(ResourceLocation.parse("video:ship"));

	@AutoInit
	public static void bootstrap() {
		VidLibTool.register(new BulkModificationToolItem());
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
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (hit != null && player.level() instanceof ServerLevel level) {
			var pos = hit.getBlockPos();
			var state = level.getBlockState(pos);

			if (player.isShiftKeyDown()) {
				var blocks = player.level().walkBlocks(ConnectedBlock.WalkType.DIAGONAL, pos, new BlockIdFilter(state.getBlock()), 1024, 1024);
				player.status("Blocks: " + blocks.size());

				var list = new ArrayList<BlockPos>();

				for (var block : blocks) {
					list.add(block.block().pos().above());
					player.level().textParticles(new TextParticleOptions(Component.literal(String.valueOf(block.distance())), 60), List.of(Vec3.atCenterOf(block.block().pos().above())));
				}

				player.level().cubeParticles(new CubeParticleOptions(Color.CYAN, -60), list);
			} else {
				level.bulkModify(true, modifications -> modify(level, modifications, pos, state));
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
