package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Icon {
	SimpleRegistry<Icon> REGISTRY = SimpleRegistry.create(Icon::type);
	Codec<Icon> CODEC = REGISTRY.valueCodec();
	StreamCodec<RegistryFriendlyByteBuf, Icon> STREAM_CODEC = REGISTRY.valueStreamCodec();
	DataType<Icon> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Icon.class);
	CommandDataType<Icon> COMMAND = CommandDataType.of(DATA_TYPE);

	SimpleRegistryType.Unit<Icon> YES = SimpleRegistryType.unit("yes", new TextureIcon(VidLib.id("textures/misc/yes.png"), UV.FULL, true, Color.WHITE));
	SimpleRegistryType.Unit<Icon> NO = SimpleRegistryType.unit("no", new TextureIcon(VidLib.id("textures/misc/no.png"), UV.FULL, true, Color.WHITE));

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(EmptyIcon.TYPE);
		REGISTRY.register(ColorIcon.TYPE);
		REGISTRY.register(TextureIcon.TYPE);
		REGISTRY.register(ItemIcon.TYPE);
		REGISTRY.register(AtlasSpriteIcon.TYPE);

		REGISTRY.register(YES);
		REGISTRY.register(NO);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default IconHolder holder() {
		return new IconHolder(this);
	}
}
