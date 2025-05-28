package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@AutoInit
public class IconHolder {
	public static final IconHolder EMPTY = new IconHolder(EmptyIcon.INSTANCE);

	public final Icon icon;
	public Object renderer;

	public IconHolder(Icon icon) {
		this.icon = icon;
	}

	public static final Codec<IconHolder> CODEC = Icon.CODEC.xmap(Icon::holder, h -> h.icon);
	public static final StreamCodec<RegistryFriendlyByteBuf, IconHolder> STREAM_CODEC = Icon.STREAM_CODEC.map(Icon::holder, h -> h.icon);
	public static final DataType<IconHolder> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, IconHolder.class);
	public static final RegisteredDataType<IconHolder> REGISTERED_DATA_TYPE = RegisteredDataType.register(VidLib.id("icon_holder"), DATA_TYPE);

	@Override
	public String toString() {
		return "IconHolder[" + icon + ']';
	}
}
