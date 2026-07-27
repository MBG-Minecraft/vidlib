package dev.latvian.mods.vidlib.feature.icon;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.registry.DynamicType;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record SimpleColorIcon(Color color) implements ColorIcon {
	public static final DynamicType<RegistryFriendlyByteBuf, Icon> TYPE = DynamicType.create(
		"color",
		"color",
		Color.CODEC,
		Color.STREAM_CODEC,
		SimpleColorIcon::new,
		SimpleColorIcon::color
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, Icon> type() {
		return TYPE;
	}
}
