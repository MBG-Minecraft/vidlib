package dev.latvian.mods.vidlib.feature.item;

import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VidLibItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(VidLib.ID);

	public static DeferredItem<Item> TOOL;

	public static void register() {
		TOOL = REGISTRY.registerSimpleItem("tool");
	}
}
