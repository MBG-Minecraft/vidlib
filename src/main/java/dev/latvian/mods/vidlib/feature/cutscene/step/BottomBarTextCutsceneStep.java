package dev.latvian.mods.vidlib.feature.cutscene.step;

import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record BottomBarTextCutsceneStep(Component text) implements CutsceneStepType {
	public static final DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> TYPE = DynamicType.create(
		"bottom_bar_text",
		"text",
		ComponentSerialization.CODEC,
		ComponentSerialization.STREAM_CODEC,
		BottomBarTextCutsceneStep::new,
		BottomBarTextCutsceneStep::text,
		Component.empty()
	);

	@Override
	public DynamicType<RegistryFriendlyByteBuf, CutsceneStepType> type() {
		return TYPE;
	}

	@Override
	public String getDisplayName() {
		return "Bottom Bar Text";
	}

	@Override
	public ImIcon getIcon() {
		return ImIcons.TEXT;
	}
}
