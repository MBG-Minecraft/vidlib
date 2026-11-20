package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record OriginCutsceneStep(Ref<KVector> position) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"origin",
		"position",
		KVector.CODEC,
		KVector.STREAM_CODEC,
		OriginCutsceneStep::new,
		OriginCutsceneStep::position
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "Origin";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.LOCATION;
	}

	@Override
	public boolean getHasSnap() {
		return true;
	}
}
