package dev.latvian.mods.vidlib.feature.camera;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2dc;

public interface ScreenShakeType {
	CustomRegistry<ByteBuf, ScreenShakeType> REGISTRY = CustomRegistry.<ByteBuf, ScreenShakeType>builder()
		.keys(ID.vidlib("screen_shake_type"), VidLib.ID)
		.type(ScreenShakeType::type)
		.build();

	Codec<Ref<ScreenShakeType>> CODEC = REGISTRY.codec();
	StreamCodec<ByteBuf, Ref<ScreenShakeType>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<ScreenShakeType>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<ByteBuf, ScreenShakeType> registry) {
		registry.register(LemniscateScreenShakeType.DEFAULT);
		registry.register(LemniscateScreenShakeType.HORIZONTAL);
		registry.register(LemniscateScreenShakeType.VERTICAL);
		registry.register(LemniscateScreenShakeType.TYPE);
	}

	@Nullable
	default CustomRegistryType<ByteBuf, ScreenShakeType> type() {
		return null;
	}

	Vector2dc get(float progress);
}
