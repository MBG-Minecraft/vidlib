package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector2dc;

public interface ScreenShakeType extends CustomRegistryValue<RegistryFriendlyByteBuf, ScreenShakeType> {
	CustomRegistry<RegistryFriendlyByteBuf, ScreenShakeType> REGISTRY = CustomRegistry.create("screen_shake_type");

	Codec<Ref<ScreenShakeType>> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Ref<ScreenShakeType>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<ScreenShakeType>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenShakeType> registry) {
		registry.register(LemniscateScreenShakeType.DEFAULT);
		registry.register(LemniscateScreenShakeType.HORIZONTAL);
		registry.register(LemniscateScreenShakeType.VERTICAL);
		registry.register(LemniscateScreenShakeType.TYPE);
	}

	@Override
	default CustomRegistry<RegistryFriendlyByteBuf, ScreenShakeType> getRegistry() {
		return REGISTRY;
	}

	Vector2dc get(float progress);
}
