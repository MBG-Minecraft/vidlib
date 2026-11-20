package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record TargetCutsceneStep(Ref<KVector> position) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"target",
		"position",
		KVector.CODEC,
		KVector.STREAM_CODEC,
		TargetCutsceneStep::new,
		TargetCutsceneStep::position
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "Target";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.TARGET;
	}

	@Override
	public boolean getHasSnap() {
		return true;
	}
}
