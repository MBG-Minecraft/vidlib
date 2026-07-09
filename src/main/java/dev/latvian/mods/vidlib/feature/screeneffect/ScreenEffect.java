package dev.latvian.mods.vidlib.feature.screeneffect;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.AngledChromaticAberrationEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.ColorOverlayEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.effect.FocusedChromaticAberrationEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public interface ScreenEffect {
	CustomRegistry<ByteBuf, ScreenEffect> REGISTRY = CustomRegistry.<ByteBuf, ScreenEffect>builder()
		.keys(ID.vidlib("screen_effect"), VidLib.ID)
		.type(ScreenEffect::type)
		.build();

	Codec<Ref<ScreenEffect>> CODEC = REGISTRY.codec();
	StreamCodec<ByteBuf, Ref<ScreenEffect>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<ScreenEffect>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<ByteBuf, ScreenEffect> registry) {
		registry.register(ColorEffect.TYPE);
		registry.register(ColorOverlayEffect.TYPE);
		registry.register(FocusedChromaticAberrationEffect.TYPE);
		registry.register(AngledChromaticAberrationEffect.TYPE);
	}

	String getName();

	ImIcon getIcon();

	@Nullable
	default CustomRegistryType<ByteBuf, ScreenEffect> type() {
		return null;
	}

	ScreenEffectInstance createInstance();
}
