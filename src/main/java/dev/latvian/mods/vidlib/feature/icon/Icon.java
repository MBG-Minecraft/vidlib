package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Icon extends SimpleRegistryEntry {
	SimpleRegistry<Icon> REGISTRY = SimpleRegistry.create(VidLib.id("icon"), c -> PlatformHelper.CURRENT.collectIcons(c));

	SimpleRegistryType.Unit<Icon> YES = SimpleRegistryType.unit("yes", new TextureIcon(VidLib.id("textures/misc/yes.png"), UV.FULL, true, Color.WHITE));
	SimpleRegistryType.Unit<Icon> NO = SimpleRegistryType.unit("no", new TextureIcon(VidLib.id("textures/misc/no.png"), UV.FULL, true, Color.WHITE));

	Codec<Icon> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Icon> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Icon> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, Icon.class);
	CommandDataType<Icon> COMMAND = CommandDataType.of(DATA_TYPE);

	static void builtinTypes(SimpleRegistryCollector<Icon> registry) {
		registry.register(EmptyIcon.TYPE);

		registry.register(ColorIcon.TYPE);
		registry.register(TextureIcon.TYPE);
		registry.register(ItemIcon.TYPE);
		registry.register(AtlasSpriteIcon.TYPE);

		registry.register(YES);
		registry.register(NO);
	}

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default IconHolder holder() {
		return new IconHolder(this);
	}
}
