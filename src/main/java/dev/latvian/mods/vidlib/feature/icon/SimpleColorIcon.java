package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record SimpleColorIcon(Color color) implements ColorIcon {
	public static final CustomRegistryType<RegistryFriendlyByteBuf, Icon> TYPE = REGISTRY.dynamic(
		ID.vidlib("color"),
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			Color.CODEC.fieldOf("color").forGetter(SimpleColorIcon::color)
		).apply(instance, SimpleColorIcon::new)),
		Color.STREAM_CODEC.map(SimpleColorIcon::new, SimpleColorIcon::color)
	);

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, Icon> type() {
		return TYPE;
	}
}
