package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.AngledChromaticAberrationEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorOverlayEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.FocusedChromaticAberrationEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ScreenEffect extends CustomRegistryValue<RegistryFriendlyByteBuf, ScreenEffect> {
	CustomRegistry<RegistryFriendlyByteBuf, ScreenEffect> REGISTRY = CustomRegistry.create("screen_effect");

	Codec<Ref<ScreenEffect>> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, Ref<ScreenEffect>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<ScreenEffect>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenEffect> registry) {
		registry.register(ColorEffect.TYPE);
		registry.register(ColorOverlayEffect.TYPE);
		registry.register(FocusedChromaticAberrationEffect.TYPE);
		registry.register(AngledChromaticAberrationEffect.TYPE);
	}

	String getName();

	ImIcon getIcon();

	@Override
	default CustomRegistry<RegistryFriendlyByteBuf, ScreenEffect> getRegistry() {
		return REGISTRY;
	}

	ScreenEffectInstance createInstance();
}
