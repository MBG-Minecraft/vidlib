package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.util.ParsedEntitySelector;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;

public class ParsedEntitySelectorImBuilder implements ImBuilder<ParsedEntitySelector> {
	public final ImString selector = ImGuiUtils.resizableString();
	public final ImBoolean single = new ImBoolean(false);
	public final ImBoolean playersOnly = new ImBoolean(false);

	@Override
	public void set(ParsedEntitySelector value) {
		selector.set(value.getInput());
		single.set(value.isSingle());
		playersOnly.set(value.isPlayersOnly());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		ImGui.alignTextToFramePadding();
		ImGui.text("Selector");
		ImGui.sameLine();
		ImGui.inputText("###selector", selector);
		update = update.orItemEdit();

		update = update.or(ImGui.checkbox("Single###single", single));
		update = update.or(ImGui.checkbox("Players Only###players-only", playersOnly));
		return update;
	}

	@Override
	public boolean isValid() {
		return new ParsedEntitySelector(selector.get(), single.get(), playersOnly.get()).getSelectorLazy().get().isSuccess();
	}

	@Override
	public ParsedEntitySelector build() {
		return new ParsedEntitySelector(selector.get(), single.get(), playersOnly.get());
	}
}
