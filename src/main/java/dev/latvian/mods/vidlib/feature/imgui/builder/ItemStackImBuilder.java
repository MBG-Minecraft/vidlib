package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.mojang.brigadier.StringReader;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemStackImBuilder implements ImBuilder<ItemStack> {
	public static final ImBuilderType<ItemStack> TYPE = () -> new ItemStackImBuilder(false);
	public static final ImBuilderType<ItemStack> TYPE_WITH_COUNT = () -> new ItemStackImBuilder(true);

	public final boolean hasCount;
	public final StringImBuilder item = new StringImBuilder();
	public final IntImBuilder count = new IntImBuilder(1, 64);
	private ItemStack result = null;

	public ItemStackImBuilder(boolean hasCount) {
		this.hasCount = hasCount;
	}

	@Override
	public void set(ItemStack value) {
		if (value != null) {
			item.set(value.isEmpty() ? "" : value.getItem().builtInRegistryHolder().getKey().location().toString());
			count.set(Math.max(1, value.getCount()));
			result = value.copy();
		} else {
			item.set("");
			count.set(1);
			result = null;
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		if (!hasCount) {
			update = item.imgui(graphics);
		} else {
			update = update.or(item.imguiKey(graphics, "Item", "item"));
			update = update.or(count.imguiKey(graphics, "Count", "count"));
		}

		if (update.isAny()) {
			try {
				var parser = new ItemParser(Minecraft.getInstance().level.registryAccess());
				var r = parser.parse(new StringReader(item.build()));
				result = r.item().value() == Items.AIR ? ItemStack.EMPTY : new ItemStack(r.item(), hasCount ? count.build() : 1, r.components());
			} catch (Exception ex) {
				result = null;
			}
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return result != null && count.isValid();
	}

	@Override
	public ItemStack build() {
		return result == null ? ItemStack.EMPTY : result;
	}
}
