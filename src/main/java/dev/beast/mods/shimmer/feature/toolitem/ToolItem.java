package dev.beast.mods.shimmer.feature.toolitem;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ToolItem {
	Map<String, ToolItem> REGISTRY = new HashMap<>();

	@Nullable
	static ToolItem of(ItemStack stack) {
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			var toolType = stack.get(DataComponents.CUSTOM_DATA).getUnsafe().getString("shimmer:tool");

			if (!toolType.isEmpty()) {
				return ToolItem.REGISTRY.get(toolType);
			}
		}

		return null;
	}

	@Nullable
	static Pair<ItemStack, ToolItem> of(LivingEntity entity) {
		var stack = entity.getMainHandItem();
		var tool = of(stack);

		if (tool != null) {
			return Pair.of(stack, tool);
		}

		stack = entity.getOffhandItem();
		tool = of(stack);

		if (tool != null) {
			return Pair.of(stack, tool);
		}

		return null;
	}

	Component getName();

	default ItemStack createItem() {
		return new ItemStack(Items.BREEZE_ROD);
	}

	default boolean useOnBlock(Player player, UseItemOnBlockEvent event) {
		return false;
	}

	default boolean useOnEntity(Player player, PlayerInteractEvent.EntityInteract event) {
		return false;
	}

	default boolean use(Player player, PlayerInteractEvent.RightClickItem event) {
		return false;
	}

	default void drawText(ItemStack item, Player player, @Nullable HitResult result, List<Component> left, List<Component> right) {
	}
}
