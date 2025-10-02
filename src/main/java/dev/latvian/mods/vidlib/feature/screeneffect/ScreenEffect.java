package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ScreenEffect {
	SimpleRegistry<ScreenEffect> REGISTRY = SimpleRegistry.create(ScreenEffect::type);
	Codec<ScreenEffect> CODEC = REGISTRY.valueCodec();
	StreamCodec<RegistryFriendlyByteBuf, ScreenEffect> STREAM_CODEC = REGISTRY.valueStreamCodec();
	DataType<ScreenEffect> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, ScreenEffect.class);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(ColorEffect.TYPE);
		REGISTRY.register(ScreenFocusedChromaticAberrationEffect.TYPE);
		REGISTRY.register(WorldFocusedChromaticAberrationEffect.TYPE);
		REGISTRY.register(AngledChromaticAberrationEffect.TYPE);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	ScreenEffectInstance createInstance();
}
