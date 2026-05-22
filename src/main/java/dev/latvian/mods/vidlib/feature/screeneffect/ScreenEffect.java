package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.AngledChromaticAberrationEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorOverlayEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.FocusedChromaticAberrationEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ScreenEffect extends SimpleRegistryEntry {
	SimpleRegistry<ScreenEffect> REGISTRY = SimpleRegistry.create(VidLib.id("screen_effect"), c -> PlatformHelper.CURRENT.collectScreenEffects(c));

	Codec<ScreenEffect> CODEC = REGISTRY.codec();
	StreamCodec<RegistryFriendlyByteBuf, ScreenEffect> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<ScreenEffect> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, ScreenEffect.class);

	static void builtinTypes(SimpleRegistryCollector<ScreenEffect> registry) {
		registry.register(ColorEffect.TYPE);
		registry.register(ColorOverlayEffect.TYPE);
		registry.register(FocusedChromaticAberrationEffect.TYPE);
		registry.register(AngledChromaticAberrationEffect.TYPE);
	}

	String getName();

	ImIcon getIcon();

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	ScreenEffectInstance createInstance();
}
