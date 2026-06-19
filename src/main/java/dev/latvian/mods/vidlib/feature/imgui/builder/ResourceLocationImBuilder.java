package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;

public class ResourceLocationImBuilder implements ImBuilder<Identifier> {
	public static final ImBuilderType<Identifier> IMMEDIATE_TYPE = () -> new ResourceLocationImBuilder(true);
	public static final ImBuilderType<Identifier> DELAYED_TYPE = () -> new ResourceLocationImBuilder(false);

	public final ImString value;
	public final boolean immediateUpdates;

	public ResourceLocationImBuilder(boolean immediateUpdates) {
		this.value = ImGuiUtils.resizableString();
		this.immediateUpdates = immediateUpdates;
	}

	@Override
	public void set(Identifier v) {
		value.set(v.toString());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ImGui.inputText("###string", value);
		var update = ImUpdate.itemEdit();
		return immediateUpdates ? update : ImUpdate.full(update.isFull());
	}

	@Override
	public boolean isValid() {
		if (value.get().isEmpty()) {
			return false;
		}

		try {
			Identifier.parse(value.get());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Identifier build() {
		return Identifier.parse(value.get());
	}
}
