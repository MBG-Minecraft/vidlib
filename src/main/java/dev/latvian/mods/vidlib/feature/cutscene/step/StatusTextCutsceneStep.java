package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record StatusTextCutsceneStep(Component text) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"status_text",
		"text",
		ComponentSerialization.CODEC,
		ComponentSerialization.STREAM_CODEC,
		StatusTextCutsceneStep::new,
		StatusTextCutsceneStep::text,
		Component.empty()
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "Status Text";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.TEXT;
	}

	@Override
	public boolean getHasSnap() {
		return true;
	}
}
