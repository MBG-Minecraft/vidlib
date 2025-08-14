package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CompoundImBuilder<T> implements ImBuilder<T> {
	public record BuilderEntry(String name, ImBuilder<?> builder, @Nullable ImBoolean optional) {
	}

	private final List<BuilderEntry> builders = new ArrayList<>();

	public void add(String name, ImBuilder<?> builder, @Nullable ImBoolean optional) {
		builders.add(new BuilderEntry(name, builder, optional));
	}

	public void add(String name, ImBuilder<?> builder) {
		add(name, builder, null);
	}

	private ImUpdate buildImGui(ImGraphics graphics, CompoundImBuilder<?> compoundImBuilder, MutableInt id) {
		var update = ImUpdate.NONE;

		for (var builder : compoundImBuilder.builders) {
			if (builder.builder instanceof CompoundImBuilder<?> child) {
				update = update.or(buildImGui(graphics, child, id));
				continue;
			}

			int i = id.getAndIncrement();

			ImGui.tableNextRow();
			ImGui.tableNextColumn();
			ImGui.alignTextToFramePadding();
			ImGui.text(builder.name);

			ImGui.tableNextColumn();

			if (builder.optional != null) {
				ImGui.checkbox("###" + i, builder.optional);
			}

			if (builder.optional == null || builder.optional.get()) {
				ImGui.pushID(i);
				ImGui.pushItemWidth(-1F);
				update = builder.builder.imgui(graphics);
				ImGui.popItemWidth();
				ImGui.popID();
			}
		}

		return update;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		var id = new MutableInt(0);

		if (ImGui.beginTable("###compound", 2, ImGuiTableFlags.SizingStretchProp | ImGuiTableFlags.BordersOuter)) {
			update = update.or(buildImGui(graphics, this, id));
			ImGui.endTable();
		}

		return update;
	}

	@Override
	public boolean isValid() {
		for (var builder : builders) {
			if ((builder.optional == null || builder.optional.get()) && !builder.builder.isValid()) {
				return false;
			}
		}

		return true;
	}
}
