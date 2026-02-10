package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataKeyStorage;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class DataMapConfigPanel extends Panel {
	private record DataMapBuilderEntry<T>(DataKey<T> key, @Nullable ImBuilder<T> builder) {
	}

	private final List<DataMapBuilderEntry<?>> entries;

	public DataMapConfigPanel(String label) {
		super("data-map-config", label);
		this.entries = new ArrayList<>();
	}

	public abstract DataKeyStorage getDataKeyStorage();

	public abstract DataMap getDataMap(Minecraft mc);

	public abstract <T> void sendUpdate(Minecraft mc, DataKey<T> key, T value);

	@Override
	public void onOpened() {
		super.onOpened();
		entries.clear();

		for (var key : getDataKeyStorage().all.values()) {
			entries.add(new DataMapBuilderEntry<>(key, key.imBuilder() == null ? null : Cast.to(key.imBuilder().get())));
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
			var dataMap = getDataMap(graphics.mc);

			for (var entry : entries) {
				ImGui.pushID(entry.key.id());
				var defaultValue = entry.key.defaultValue();
				var currentValue = dataMap.get(entry.key, graphics.mc.level.getGameTime());
				boolean isDefault = graphics.isReplay ? !dataMap.hasSuperOverride(entry.key) : Objects.equals(defaultValue, currentValue);

				if (entry.builder != null) {
					try {
						entry.builder.set(Cast.to(currentValue));
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				}

				ImGui.tableNextRow();

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				if (isDefault) {
					ImGui.beginDisabled();
				}

				if (ImGui.smallButton(ImIcons.UNDO + "###reset")) {
					if (graphics.isReplay) {
						dataMap.removeSuperOverride(entry.key);

						if (entry.builder != null) {
							entry.builder.set(Cast.to(dataMap.get(entry.key)));
						}
					} else {
						dataMap.set(entry.key, Cast.to(defaultValue));

						if (entry.builder != null) {
							entry.builder.set(Cast.to(defaultValue));
						}
					}

					if (!graphics.isReplay) {
						sendUpdate(graphics.mc, entry.key, Cast.to(defaultValue));
					}
				}

				if (!isDefault && ImGui.isItemHovered()) {
					if (entry.builder != null) {
						ImGui.setTooltip("Reset to " + entry.builder.toString(graphics.mc.level.jsonOps(), Cast.to(defaultValue)));
					} else {
						try {
							ImGui.setTooltip("Reset to " + entry.key.type().codec().encodeStart(graphics.mc.level.jsonOps(), Cast.to(defaultValue)).getOrThrow());
						} catch (Exception ex) {
							ImGui.setTooltip("Reset to the default value");
						}
					}
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

				if (entry.builder == null) {
					try {
						var string = entry.key.type().codec().encodeStart(graphics.mc.level.jsonOps(), Cast.to(dataMap.get(entry.key))).getOrThrow().toString();

						if (string.length() <= 50) {
							ImGui.text(string);
						} else {
							ImGui.button("JSON###value-json");

							if (ImGui.isItemHovered()) {
								ImGui.setTooltip(string);
							}
						}
					} catch (Exception ex) {
						graphics.redTextIf("Unable to encode JSON", true);
					}
				}

				var update = entry.builder == null ? ImUpdate.NONE : entry.builder.imgui(graphics);
				ImGui.popID();

				if (entry.builder != null && update.isAny() && entry.builder.isValid()) {
					var value = entry.builder.build();

					if (graphics.isReplay) {
						dataMap.setSuperOverride(entry.key, value);
					} else {
						dataMap.set(entry.key, Cast.to(value));
					}

					if (!graphics.isReplay && update.isFull()) {
						sendUpdate(graphics.mc, entry.key, Cast.to(value));
					}
				}

				ImGui.popItemWidth();
				ImGui.popID();
			}

			ImGui.endTable();
		}
	}
}
