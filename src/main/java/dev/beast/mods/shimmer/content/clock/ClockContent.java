package dev.beast.mods.shimmer.content.clock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public interface ClockContent {
	DeferredBlock<ClockBlock> BLOCK = Shimmer.BLOCKS.registerBlock("clock", ClockBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS));
	DeferredItem<BlockItem> ITEM = Shimmer.ITEMS.registerSimpleBlockItem(BLOCK);
	DeferredHolder<BlockEntityType<?>, BlockEntityType<ClockBlockEntity>> BLOCK_ENTITY = Shimmer.BLOCK_ENTITIES.register("clock", () -> BlockEntityType.Builder.of(ClockBlockEntity::new, BLOCK.get()).build(null));

	static void init() {
	}

	static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
		return Commands.literal("clock")
			.then(Commands.argument("ticks", IntegerArgumentType.integer(0, ClockBlockEntity.MAX_TICKS))
				.executes(ctx -> clock(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "ticks")))
			);
	}

	private static int clock(CommandSourceStack source, int ticks) {
		source.getServer().send(new ClockPayload(ticks, -1));
		return 1;
	}
}
