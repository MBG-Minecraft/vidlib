package dev.latvian.mods.vidlib.feature.icon;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.UnitType;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Icon extends CustomRegistryValue<RegistryFriendlyByteBuf, Icon> {
	CustomRegistry<RegistryFriendlyByteBuf, Icon> REGISTRY = CustomRegistry.create("icon");

	Codec<Ref<Icon>> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Ref<Icon>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<Icon>> DATA_TYPE = REGISTRY.dataType();

	UnitType<RegistryFriendlyByteBuf, Icon> EMPTY = UnitType.create("empty", EmptyIcon.INSTANCE);
	UnitType<RegistryFriendlyByteBuf, Icon> YES = UnitType.create("yes", new TextureIcon(VidLibTextures.YES, UV.FULL, true, Color.WHITE));
	UnitType<RegistryFriendlyByteBuf, Icon> NO = UnitType.create("no", new TextureIcon(VidLibTextures.NO, UV.FULL, true, Color.WHITE));

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, Icon> registry) {
		registry.register(EMPTY);

		registry.register(SimpleColorIcon.TYPE);
		registry.register(TextureIcon.TYPE);
		registry.register(ItemIcon.TYPE);
		registry.register(AtlasSpriteIcon.TYPE);

		registry.register(YES);
		registry.register(NO);
	}

	@Override
	default CustomRegistry<RegistryFriendlyByteBuf, Icon> getRegistry() {
		return REGISTRY;
	}
}
