package dev.beast.mods.shimmer.feature.icon;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class IconHolder {
	public static final IconHolder EMPTY = new IconHolder(EmptyIcon.INSTANCE);

	public final Icon icon;
	public Object renderer;

	public IconHolder(Icon icon) {
		this.icon = icon;
	}

	public static final Codec<IconHolder> CODEC = Icon.CODEC.xmap(Icon::holder, h -> h.icon);
	public static final StreamCodec<RegistryFriendlyByteBuf, IconHolder> STREAM_CODEC = Icon.STREAM_CODEC.map(Icon::holder, h -> h.icon);

	@Override
	public String toString() {
		return "IconHolder[" + icon + ']';
	}
}
