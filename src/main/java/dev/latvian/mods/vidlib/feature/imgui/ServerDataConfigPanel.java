package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerDataConfigPanel extends AdminPanel {
	public static final ServerDataConfigPanel INSTANCE = new ServerDataConfigPanel();

	private record ServerDataBuilderEntry<T>(DataKey<T> key, ImBuilder<T> builder) {
	}

	private final List<ServerDataBuilderEntry<?>> entries;

	private ServerDataConfigPanel() {
		super("server-data-config", "Server Data");
		this.entries = new ArrayList<>();
	}

	@Override
	public void onOpened() {
		super.onOpened();
		entries.clear();

		for (var key : DataKey.SERVER.all.values()) {
			if (key.imBuilder() != null) {
				entries.add(new ServerDataBuilderEntry<>(key, Cast.to(key.imBuilder().get())));
			}
		}

		entries.sort((a, b) -> a.key.id().compareToIgnoreCase(b.key.id()));
	}

	@Override
	public void content(ImGraphics graphics) {
		if (!graphics.inGame) {
			close();
			return;
		}

		if (ImGui.beginTable("###data", 3, ImGuiTableFlags.SizingStretchProp | ImGuiTableFlags.Borders)) {
			for (var entry : entries) {
				ImGui.pushID(entry.key.id());
				var defaultValue = entry.key.defaultValue();
				var currentValue = graphics.mc.level.get(entry.key);
				boolean isDefault = Objects.equals(defaultValue, currentValue);

				try {
					entry.builder.set(Cast.to(currentValue));
				} catch (Throwable ex) {
					ex.printStackTrace();
				}

				ImGui.tableNextRow();

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				if (isDefault) {
					ImGui.beginDisabled();
				}

				if (ImGui.smallButton(ImIcons.UNDO + "###reset")) {
					entry.builder.set(Cast.to(defaultValue));
					graphics.mc.getServerData().set(entry.key, Cast.to(defaultValue));

					if (!graphics.isClientOnly) {
						graphics.mc.updateServerDataValue(entry.key, Cast.to(defaultValue));
					}
				}

				if (!isDefault && ImGui.isItemHovered()) {
					ImGui.setTooltip("Reset to " + entry.builder.toString(graphics.mc.level.jsonOps(), Cast.to(defaultValue)));
				}

				if (isDefault) {
					ImGui.endDisabled();
				}

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				if (!isDefault) {
					graphics.pushStack();
					graphics.setWarningText();
				}

				ImGui.text(entry.key.id());

				if (!isDefault) {
					graphics.popStack();
				}

				ImGui.tableNextColumn();
				ImGui.pushItemWidth(-1F);

				ImGui.pushID("###value");
				var update = entry.builder.imgui(graphics);
				ImGui.popID();

				if (update.isAny() && entry.builder.isValid()) {
					var value = entry.builder.build();
					graphics.mc.getServerData().set(entry.key, Cast.to(value));

					if (!graphics.isClientOnly && update.isFull()) {
						graphics.mc.updateServerDataValue(entry.key, Cast.to(value));
					}
				}

				ImGui.popItemWidth();
				ImGui.popID();
			}

			ImGui.endTable();
		}
	}
}
