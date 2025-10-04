package dev.latvian.mods.vidlib.feature.item;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLibContent;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.auto.ServerCommandHolder;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface VidLibTool {
	Lazy<Map<String, VidLibTool>> REGISTRY = Lazy.map(map -> {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof VidLibTool tool) {
				map.put(tool.getId(), tool);
			}
		}
	});

	@AutoRegister
	ServerCommandHolder COMMAND = new ServerCommandHolder("vidlib-tool", (command, buildContext) -> {
		command.requires(source -> source.hasPermission(2));

		for (var tool : REGISTRY.get().values()) {
			var cmd = Commands.literal(tool.getId().replace('_', '-'));
			tool.registerCommands(cmd, buildContext);

			if (!cmd.getArguments().isEmpty()) {
				command.then(cmd);
			}
		}
	});

	@Nullable
	static VidLibTool of(ItemStack stack) {
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			var toolType = stack.get(DataComponents.CUSTOM_DATA).getUnsafe().getStringOr("vidlib:tool", "");

			if (!toolType.isEmpty()) {
				return VidLibTool.REGISTRY.get().get(toolType);
			}
		}

		return null;
	}

	@Nullable
	static Pair<ItemStack, VidLibTool> of(LivingEntity entity) {
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

	static boolean isHolding(LivingEntity entity, VidLibTool tool) {
		return of(entity.getMainHandItem()) == tool || of(entity.getOffhandItem()) == tool;
	}

	String getId();

	Component getName();

	@Nullable
	default ResourceLocation getModel() {
		return null;
	}

	default ItemStack createFullItem() {
		var stack = new ItemStack(VidLibContent.Items.TOOL.get());
		var tag = new CompoundTag();
		tag.putString("vidlib:tool", getId());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		stack.set(DataComponents.ITEM_NAME, getName());
		stack.set(DataComponents.RARITY, Rarity.EPIC);
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		stack.set(DataComponents.MAX_STACK_SIZE, 1);

		var model = getModel();

		if (model != null) {
			stack.set(DataComponents.ITEM_MODEL, model);
		}

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

	default void renderSetup(Player player, ItemStack item, @Nullable HitResult hit, float delta) {
	}

	default void debugText(Player player, ItemStack item, @Nullable HitResult hit, ScreenText screenText) {
	}

	default void visuals(Player player, ItemStack item, Visuals visuals, float delta) {
	}
}
