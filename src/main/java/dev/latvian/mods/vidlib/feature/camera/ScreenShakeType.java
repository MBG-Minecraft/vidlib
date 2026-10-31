package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.Ref;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector2dc;

public interface ScreenShakeType extends CustomRegistryValue<ByteBuf, ScreenShakeType> {
	CustomRegistry<ByteBuf, ScreenShakeType> REGISTRY = CustomRegistry.create("screen_shake_type");

	Codec<Ref<ScreenShakeType>> CODEC = REGISTRY.codec();
	StreamCodec<ByteBuf, Ref<ScreenShakeType>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<ScreenShakeType>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<ByteBuf, ScreenShakeType> registry) {
		registry.register(LemniscateScreenShakeType.DEFAULT);
		registry.register(LemniscateScreenShakeType.HORIZONTAL);
		registry.register(LemniscateScreenShakeType.VERTICAL);
		registry.register(LemniscateScreenShakeType.TYPE);
	}

	@Override
	default CustomRegistry<ByteBuf, ScreenShakeType> getRegistry() {
		return REGISTRY;
	}

	Vector2dc get(float progress);
}
