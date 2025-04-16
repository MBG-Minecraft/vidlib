package dev.beast.mods.shimmer.feature.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.feature.auto.ServerCommandHolder;
import dev.beast.mods.shimmer.feature.misc.ScreenText;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface ShimmerTool {
	Map<String, ShimmerTool> REGISTRY = new HashMap<>();

	static void register(ShimmerTool tool) {
		REGISTRY.put(tool.getId(), tool);
	}

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("vidlib-tool", (command, buildContext) -> {
		command.requires(source -> source.hasPermission(2));

		for (var tool : REGISTRY.values()) {
			var cmd = Commands.literal(tool.getId().replace('_', '-'));
			tool.registerCommands(cmd, buildContext);

			if (!cmd.getArguments().isEmpty()) {
				command.then(cmd);
			}
		}
	});

	@Nullable
	static ShimmerTool of(ItemStack stack) {
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			var toolType = stack.get(DataComponents.CUSTOM_DATA).getUnsafe().getStringOr("vidlib:tool", "");

			if (!toolType.isEmpty()) {
				return ShimmerTool.REGISTRY.get(toolType);
			}
		}

		return null;
	}

	@Nullable
	static Pair<ItemStack, ShimmerTool> of(LivingEntity entity) {
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

	static boolean isHolding(LivingEntity entity, Class<?> toolClass) {
		return toolClass.isInstance(of(entity.getMainHandItem())) || toolClass.isInstance(of(entity.getOffhandItem()));
	}

	String getId();

	Component getName();

	ItemStack createItem();

	default ItemStack createFullItem() {
		var stack = createItem();
		stack.set(DataComponents.ITEM_NAME, getName());
		var tag = new CompoundTag();
		tag.putString("vidlib:tool", getId());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return stack;
	}

	default void registerCommands(LiteralArgumentBuilder<CommandSourceStack> command, CommandBuildContext buildContext) {
	}

	default boolean useOnBlock(Player player, ItemStack item, UseItemOnBlockEvent event) {
		return rightClick(player, item, event.getUseOnContext().getHitResult().getType() == HitResult.Type.BLOCK ? event.getUseOnContext().getHitResult() : null);
	}

	default boolean use(Player player, ItemStack item) {
		return rightClick(player, item, player.ray(500D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY));
	}

	default boolean useOnEntity(Player player, ItemStack item, Entity target) {
		return false;
	}

	default boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		return true;
	}

	default boolean leftClick(Player player, ItemStack item) {
		return true;
	}

	default void debugText(Player player, ItemStack item, @Nullable HitResult hit, ScreenText screenText) {
	}

	default ToolVisuals visuals(Player player, ItemStack item, float delta) {
		return ToolVisuals.NONE;
	}
}
