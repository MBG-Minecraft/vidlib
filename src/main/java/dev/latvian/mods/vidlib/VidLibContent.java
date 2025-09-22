package dev.latvian.mods.vidlib;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface VidLibContent {
	interface Blocks {
		DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VidLib.ID);
	}

	interface Items {
		DeferredRegister.Items ITEMS = DeferredRegister.createItems(VidLib.ID);

		DeferredItem<Item> TOOL = ITEMS.registerSimpleItem("tool");
	}

	static void init(IEventBus bus) {
		Blocks.BLOCKS.register(bus);
		Items.ITEMS.register(bus);
	}
}
