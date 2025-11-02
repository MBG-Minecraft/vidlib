package dev.latvian.mods.vidlib.feature.client;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.vidlib.util.TextIcons;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.LinkedHashMap;
import java.util.List;

public class ClientItemTooltips {
	private static String reduce(ResourceLocation id) {
		return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
	}

	public static void onItemTooltip(ItemTooltipEvent event) {
		var mc = Minecraft.getInstance();

		if (mc.level == null) {
			return;
		}

		var flags = event.getFlags();

		if (!flags.isAdvanced()) {
			return;
		}

		var stack = event.getItemStack();

		if (stack.isEmpty() || ModList.get().isLoaded("kubejs")) {
			return;
		}

		var lines = event.getToolTip();

		if (Screen.hasAltDown()) {
			var components = BuiltInRegistries.DATA_COMPONENT_TYPE;
			var ops = mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE);

			for (var entry : stack.getComponentsPatch().entrySet()) {
				var id = components.getKey(entry.getKey());

				if (id != null) {
					var line = Component.empty();
					line.append(TextIcons.icon(Component.literal("Q.")));

					if (entry.getValue().isEmpty()) {
						line.append(Component.literal("!"));
					}

					line.append(Component.literal(reduce(id)).withStyle(ChatFormatting.YELLOW));

					if (entry.getValue().isPresent()) {
						line.append(Component.literal("="));
						var errors0 = appendComponentValue(ops, line, (DataComponentType) entry.getKey(), entry.getValue().get());

						if (!errors0.isEmpty()) {
							lines.add(Component.literal(reduce(id) + " errored, see log").withStyle(ChatFormatting.DARK_RED));
						}
					}

					lines.add(line);
				}
			}

			if (Screen.hasShiftDown()) {
				for (var type : stack.getPrototype()) {
					var id = components.getKey(type.type());

					if (id != null && stack.getComponentsPatch().get(type.type()) == null) {
						var line = Component.empty();
						line.append(TextIcons.icon(Component.literal("P.")));
						line.append(Component.literal(reduce(id)).withStyle(ChatFormatting.GRAY));
						line.append(Component.literal("="));
						var errors0 = appendComponentValue(ops, line, (DataComponentType) type.type(), type.value());

						if (!errors0.isEmpty()) {
							lines.add(Component.literal(reduce(id) + " errored, see log").withStyle(ChatFormatting.DARK_RED));
						}

						lines.add(line);
					}
				}
			}
		} else if (Screen.hasShiftDown()) {
			var fuel = stack.getBurnTime(null, mc.level.fuelValues());

			if (fuel > 0) {
				var line = Component.empty();
				line.append(TextIcons.icon(Component.literal("R.")));
				line.append(Component.literal("Fuel: ").withStyle(ChatFormatting.GOLD));

				var s = String.valueOf(fuel / 20F);

				line.append(Component.literal(fuel + " t / " + (s.endsWith(".0") ? s.substring(0, s.length() - 2) : s) + " s").withStyle(ChatFormatting.YELLOW));
				lines.add(line);
			}

			var tempTagNames = new LinkedHashMap<ResourceLocation, TagInstance>();
			TagInstance.Type.ITEM.append(tempTagNames, stack.getItem().builtInRegistryHolder().tags());

			if (stack.getItem() instanceof BlockItem item) {
				TagInstance.Type.BLOCK.append(tempTagNames, item.getBlock().builtInRegistryHolder().tags());
			}

			if (stack.getItem() instanceof BucketItem bucket) {
				Fluid fluid = bucket.content;

				if (fluid != Fluids.EMPTY) {
					TagInstance.Type.FLUID.append(tempTagNames, fluid.builtInRegistryHolder().tags());
				}
			}

			if (stack.getItem() instanceof SpawnEggItem item) {
				var entityType = item.getType(mc.level.registryAccess(), stack);

				if (entityType != null) {
					TagInstance.Type.ENTITY.append(tempTagNames, entityType.builtInRegistryHolder().tags());
				}
			}

			if (!tempTagNames.isEmpty()) {
				tempTagNames.values().stream().sorted().map(TagInstance::toText).forEach(lines::add);
			}
		}
	}

	private static <T> List<String> appendComponentValue(DynamicOps<Tag> ops, MutableComponent line, DataComponentType<T> type, T value) {
		if (value == null) {
			line.append(Component.literal("null").withStyle(ChatFormatting.RED));
			return List.of();
		} else if (value instanceof Component c) {
			line.append(Component.empty().withStyle(ChatFormatting.GOLD).append(c));
		}

		try {
			var tag = type.codecOrThrow().encodeStart(ops, value).getOrThrow();
			line.append(NbtUtils.toPrettyComponent(tag));
			return List.of();
		} catch (Throwable ex) {
			line.append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.RED));
			return List.of();
		}
	}
}
