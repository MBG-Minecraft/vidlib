package dev.latvian.mods.vidlib.feature.screeneffect.effect;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record OverlayColorEffect(Ref<Gradient> color) implements ScreenEffect {
	public static final DynamicType<RegistryFriendlyByteBuf, ScreenEffect> TYPE = DynamicType.create(
		"overlay_color",
		"color",
		Gradient.CODEC,
		Gradient.STREAM_CODEC,
		OverlayColorEffect::new,
		OverlayColorEffect::color
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ScreenEffect> type() {
		return TYPE;
	}
}
