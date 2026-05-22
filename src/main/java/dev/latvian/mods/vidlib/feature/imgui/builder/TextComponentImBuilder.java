package dev.latvian.mods.vidlib.feature.imgui.builder;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.klib.util.JsonUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.PlainTextContents;

public class TextComponentImBuilder implements ImBuilder<Component> {
	public static final ImBuilderType<Component> TYPE = () -> new TextComponentImBuilder(false);
	public static final ImBuilderType<Component> MULTILINE_TYPE = () -> new TextComponentImBuilder(true);

	public final boolean multiline;
	public final ImString text = ImGuiUtils.resizableString();

	public TextComponentImBuilder(boolean multiline) {
		this.multiline = multiline;
	}

	private boolean setMultiline(Component c) {
		if (multiline && c.getContents() == PlainTextContents.EMPTY && c.getSiblings().size() % 2 == 1 && c.getStyle().isEmpty()) {
			for (int i = 1; i < c.getSiblings().size(); i += 2) {
				var s = c.getSiblings().get(i);

				if (!(s.getContents() instanceof PlainTextContents pc) || !pc.text().equals("\n") || !s.getSiblings().isEmpty() || !s.getStyle().isEmpty()) {
					return false;
				}
			}

			var builder = new StringBuilder();

			for (int i = 0; i < c.getSiblings().size(); i += 2) {
				if (i > 0) {
					builder.append('\n');
				}

				builder.append(encode(c.getSiblings().get(i)));
			}

			text.set(builder.toString());
			return true;
		}

		return false;
	}

	@Override
	public void set(Component c) {
		text.clear();

		if (c == null) {
			return;
		}

		if (!setMultiline(c)) {
			text.set(encode(c));
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;

		if (multiline) {
			ImGui.inputTextMultiline("###text", text);
		} else {
			ImGui.inputText("###text", text);
		}

		return update.orItemEdit();
	}

	public static String encode(Component c) {
		if (c.getContents() == PlainTextContents.EMPTY && c.getSiblings().isEmpty() && c.getStyle().isEmpty()) {
			return "";
		} else if (c.getContents() instanceof PlainTextContents pc && c.getSiblings().isEmpty() && c.getStyle().isEmpty()) {
			return pc.text();
		} else {
			try {
				return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, c).getOrThrow().toString();
			} catch (Exception ex) {
				return c.getString();
			}
		}
	}

	public static Component parse(String text) {
		if (text.isEmpty()) {
			return Component.empty();
		} else {
			char c = text.charAt(0);

			if (c == '"' || c == '[' || c == '{') {
				// TODO: Add support for that one mod that lets you use HTML tags in components
				try {
					return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonUtils.GSON.fromJson(text, JsonElement.class)).getOrThrow();
				} catch (Exception ex) {
					return Component.literal(text);
				}
			} else {
				return Component.literal(text);
			}
		}
	}

	@Override
	public Component build() {
		if (text.get().isEmpty()) {
			return Component.empty();
		}

		if (multiline) {
			var result = Component.empty();
			var lines = text.get().split("\n");

			for (int i = 0; i < lines.length; i++) {
				if (i > 0) {
					result.append(Component.literal("\n"));
				}

				result.append(parse(lines[i]));
			}

			return result;
		} else {
			return parse(text.get());
		}
	}
}
