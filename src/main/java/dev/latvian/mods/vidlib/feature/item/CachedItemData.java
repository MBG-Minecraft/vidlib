package dev.latvian.mods.vidlib.feature.item;

import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import dev.latvian.mods.vidlib.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record CachedItemData(ItemStack stack, ItemKey key, VisualItemKey visualKey, String search, List<List<FormattedCharSinkPartBuilder.Part>> tooltip) {
	public record Context(Minecraft mc, RegistryAccess registryAccess, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
		public Context(Minecraft mc) {
			this(
				mc,
				mc.level == null ? MiscUtils.STATIC_REGISTRY_ACCESS : mc.level.registryAccess(),
				mc.level == null ? Item.TooltipContext.of(MiscUtils.STATIC_REGISTRY_ACCESS) : Item.TooltipContext.of(mc.level),
				mc.player,
				mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
			);
		}
	}

	public static final CachedItemData AIR = new CachedItemData(ItemStack.EMPTY, new ItemKey(Items.AIR.builtInRegistryHolder(), DataComponentPatch.EMPTY), VisualItemKey.AIR, "air", List.of(List.of(new FormattedCharSinkPartBuilder.Part("Air", Style.EMPTY))));

	public static CachedItemData create(Minecraft mc, ItemStack stack, ItemKey key, Context context) {
		var sink = new FormattedCharSinkPartBuilder();
		var tooltip = new ArrayList<List<FormattedCharSinkPartBuilder.Part>>();

		for (var component : stack.getTooltipLines(context.tooltipContext, context.player, context.tooltipFlag)) {
			for (var line : mc.font.split(component, Integer.MAX_VALUE)) {
				line.accept(sink);
				tooltip.add(sink.build());
			}
		}

		return new CachedItemData(stack, key, VisualItemKey.of(stack, context.registryAccess), stack.getHoverName().getString().replace(" ", "").toLowerCase(Locale.ROOT), List.copyOf(tooltip));
	}

	public boolean matches(CachedItemData item) {
		return this == item || item != null && key.equals(item.key);
	}
}
