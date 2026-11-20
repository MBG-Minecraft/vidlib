package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record ScreenEffectCutsceneStep(Ref<ScreenEffect> effect) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"screen_effect",
		"effect",
		ScreenEffect.CODEC,
		ScreenEffect.STREAM_CODEC,
		ScreenEffectCutsceneStep::new,
		ScreenEffectCutsceneStep::effect
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "Screen Effect";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.BLUR;
	}
}
