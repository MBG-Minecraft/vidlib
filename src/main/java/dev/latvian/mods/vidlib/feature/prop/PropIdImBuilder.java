package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import imgui.ImGui;
import imgui.type.ImString;

public class PropIdImBuilder implements ImBuilder<Integer> {
	public static final ImBuilderType<Integer> TYPE = PropIdImBuilder::new;

	public final ImString id = new ImString(8);

	public PropIdImBuilder() {
		id.inputData.allowedChars = "0123456789ABCDEFabcdef";
	}

	@Override
	public void set(Integer value) {
		id.set(value == null ? "00000000" : "%08X".formatted(value));
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.inputText("###prop-id", id);
		return ImUpdate.itemEdit();
	}

	@Override
	public boolean isValid() {
		return id.getLength() == 8;
	}

	@Override
	public Integer build() {
		return id.getLength() == 8 ? Integer.parseUnsignedInt(id.get(), 16) : 0;
	}
}
