package dev.latvian.mods.vidlib.feature.item;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ItemScreen {
	Map<Item, ItemScreen> ITEMS = new Object2ObjectOpenHashMap<>();
	Map<String, ItemScreen> TOOLS = new Object2ObjectOpenHashMap<>();

	@Nullable
	static Screen create(Player player, ItemStack stack, InteractionHand hand) {
		var tool = VidLibTool.of(stack);

		if (tool != null) {
			var s = TOOLS.get(tool.getId());

			if (s != null) {
				var screen = s.createScreen(player, stack, hand);

				if (screen != null) {
					return screen;
				}
			}
		}

		var s = ITEMS.get(stack.getItem());

		if (s != null) {
			return s.createScreen(player, stack, hand);
		}

		return null;
	}

	@Nullable
	Screen createScreen(Player player, ItemStack stack, InteractionHand hand);
}
