package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.DynamicType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record ItemIcon(ItemStack stack) implements Icon {
	public static final DynamicType<RegistryFriendlyByteBuf, Icon> TYPE = DynamicType.create(
		"item",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			ItemStack.CODEC.fieldOf("item").forGetter(ItemIcon::stack)
		).apply(instance, ItemIcon::new)),
		ItemStack.STREAM_CODEC.map(ItemIcon::new, ItemIcon::stack)
	);

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, Icon> type() {
		return TYPE;
	}
}
