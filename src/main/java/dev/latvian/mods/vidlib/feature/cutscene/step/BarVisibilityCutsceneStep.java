package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record BarVisibilityCutsceneStep(Ref<KNumber> visibility) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"bar_visibility",
		"visibility",
		KNumber.CODEC,
		KNumber.STREAM_CODEC,
		BarVisibilityCutsceneStep::new,
		BarVisibilityCutsceneStep::visibility,
		KNumber.ONE
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "Bar Visibility";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.SPLIT_SCREEN;
	}

	@Override
	public boolean getHasSnap() {
		return true;
	}
}
