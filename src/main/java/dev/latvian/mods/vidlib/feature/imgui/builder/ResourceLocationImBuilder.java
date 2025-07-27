package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ResourceLocationImBuilder implements ImBuilder<ResourceLocation> {
	public final ImString value;
	public final boolean immediateUpdates;

	public ResourceLocationImBuilder(@Nullable ResourceLocation def, boolean immediateUpdates) {
		this.value = ImGuiUtils.resizableString();
		this.immediateUpdates = immediateUpdates;

		if (def != null) {
			this.value.set(def);
		}
	}

	@Override
	public void set(ResourceLocation v) {
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
			ResourceLocation.parse(value.get());
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public ResourceLocation build() {
		return ResourceLocation.parse(value.get());
	}
}
