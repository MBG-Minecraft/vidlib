package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record SoundCutsceneStep(PositionedSoundData sound) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"sound",
		"sound",
		PositionedSoundData.CODEC,
		PositionedSoundData.STREAM_CODEC,
		SoundCutsceneStep::new,
		SoundCutsceneStep::sound
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "Sound";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.PLAY;
	}
}
