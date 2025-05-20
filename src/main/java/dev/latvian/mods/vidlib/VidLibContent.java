package dev.latvian.mods.vidlib;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface VidLibContent {
	interface Blocks {
		DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VidLib.ID);

		DeferredBlock<Block> MONEY_BLOCK = BLOCKS.registerSimpleBlock("money_block");
		DeferredBlock<Block> BANDED_MONEY_BLOCK = BLOCKS.registerSimpleBlock("banded_money_block");
	}

	interface Items {
		DeferredRegister.Items ITEMS = DeferredRegister.createItems(VidLib.ID);

		DeferredItem<BlockItem> MONEY_BLOCK = ITEMS.registerSimpleBlockItem(Blocks.MONEY_BLOCK);
		DeferredItem<BlockItem> BANDED_MONEY_BLOCK = ITEMS.registerSimpleBlockItem(Blocks.BANDED_MONEY_BLOCK);

		DeferredItem<Item> MONEY = ITEMS.registerSimpleItem("money");
		DeferredItem<Item> BANDED_MONEY = ITEMS.registerSimpleItem("banded_money");
		DeferredItem<Item> FLAT_MONEY = ITEMS.registerSimpleItem("flat_money");

		DeferredItem<Item> TOOL = ITEMS.registerSimpleItem("tool");
		DeferredItem<Item> HEART = ITEMS.registerSimpleItem("heart");
		DeferredItem<Item> CROWBAR = ITEMS.registerSimpleItem("crowbar");
	}

	static void init(IEventBus bus) {
		Blocks.BLOCKS.register(bus);
		Items.ITEMS.register(bus);
	}
}
