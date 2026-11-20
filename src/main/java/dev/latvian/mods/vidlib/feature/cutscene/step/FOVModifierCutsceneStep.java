package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record FOVModifierCutsceneStep(Ref<KNumber> fov) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"fov_modifier",
		"fov",
		KNumber.CODEC,
		KNumber.STREAM_CODEC,
		FOVModifierCutsceneStep::new,
		FOVModifierCutsceneStep::fov,
		KNumber.ONE
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "FOV Modifier";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.APERTURE;
	}

	@Override
	public boolean getHasSnap() {
		return true;
	}
}
