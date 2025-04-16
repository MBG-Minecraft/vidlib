package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.item.ItemStack;

public record ItemIcon(ItemStack stack) implements Icon {
	public static final SimpleRegistryType<ItemIcon> TYPE = SimpleRegistryType.dynamic(VidLib.id("item"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ItemStack.CODEC.fieldOf("item").forGetter(ItemIcon::stack)
	).apply(instance, ItemIcon::new)), ItemStack.STREAM_CODEC.map(ItemIcon::new, ItemIcon::stack));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}
}
