package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextComponentImBuilder implements ImBuilder<Component> {
	public final TextComponentImBuilder parent;
	public final ImString text = ImGuiUtils.resizableString();
	public final List<TextComponentImBuilder> siblings = new ArrayList<>(0);
	public boolean delete = false;

	private TextComponentImBuilder(TextComponentImBuilder parent) {
		this.parent = parent;
	}

	public TextComponentImBuilder() {
		this(null);
	}

	@Override
	public void set(Component c) {
		text.clear();

		c.getContents().visit(content -> {
			text.set(content);
			return Optional.empty();
		});

		siblings.clear();

		for (var s : c.getSiblings()) {
			var sibling = new TextComponentImBuilder(this);
			sibling.set(s);
			siblings.add(sibling);
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		delete = false;
		ImGui.inputText("###text", text);
		update = update.orItemEdit();

		if (!siblings.isEmpty()) {
			ImGui.indent();

			for (int i = 0; i < siblings.size(); i++) {
				var child = siblings.get(i);
				ImGui.pushID(i);
				update = update.or(child.imgui(graphics));
				ImGui.popID();
			}

			ImGui.unindent();
		}

		return update;
	}

	@Override
	public Component build() {
		if (text.get().isEmpty() && siblings.isEmpty()) {
			return Component.empty();
		}

		return Component.literal(text.get());
	}
}
