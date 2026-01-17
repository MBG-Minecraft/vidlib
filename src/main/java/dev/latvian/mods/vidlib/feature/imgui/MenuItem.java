package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.vidlib.feature.imgui.config.VideoConfigPanel;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.util.LevelOfDetailValue;
import imgui.ImGui;
import imgui.internal.flag.ImGuiItemFlags;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record MenuItem(ImIcon icon, ImText label, ImText tooltip, String shortcut, int flags, @Nullable OnClick onClick, @Nullable Function<ImGraphics, List<MenuItem>> subItems) {
	public static final int FLAG_SEPARATOR = 1 << 0;
	public static final int FLAG_MENU = 1 << 1;
	public static final int FLAG_CHECKMARK = 1 << 2;
	public static final int FLAG_DISABLED = 1 << 3;
	public static final int FLAG_REMAIN_OPEN_OVERRIDE = 1 << 4;
	public static final int FLAG_REMAIN_OPEN = 1 << 5;
	public static final int FLAG_SKIP = 1 << 6;
	public static final int FLAG_CUSTOM_IMGUI = 1 << 7;

	public static final MenuItem SEPARATOR = text(ImIcon.NONE, "").withFlags(FLAG_SEPARATOR);
	public static final MenuItem SKIP = text(ImIcon.NONE, "").withFlags(FLAG_SKIP);

	@FunctionalInterface
	public interface OnClick {
		void onClick(ImGraphics graphics);
	}

	public static MenuItem text(ImIcon icon, ImText label) {
		return new MenuItem(icon, label, ImText.EMPTY, null, 0, null, null);
	}

	public static MenuItem text(ImIcon icon, String label) {
		return new MenuItem(icon, ImText.of(label), ImText.EMPTY, null, 0, null, null);
	}

	public static MenuItem item(ImIcon icon, String label, OnClick onClick) {
		return new MenuItem(icon, ImText.of(label), ImText.EMPTY, null, 0, onClick, null);
	}

	public static MenuItem item(String label, OnClick onClick) {
		return item(ImIcon.NONE, label, onClick);
	}

	public static MenuItem item(ImIcon icon, String label, boolean checkmark, OnClick onClick) {
		return new MenuItem(icon, ImText.of(label), ImText.EMPTY, null, checkmark ? FLAG_CHECKMARK : 0, onClick, null);
	}

	public static MenuItem item(String label, boolean checkmark, OnClick onClick) {
		return item(ImIcon.NONE, label, checkmark, onClick);
	}

	public static MenuItem item(ImIcon icon, String label, ImBoolean flag) {
		return item(icon, label, flag.get(), g -> flag.set(!flag.get()));
	}

	public static MenuItem item(ImIcon icon, String label, AdminPanel panel) {
		return item(icon, label, panel.isOpen(), g -> {
			if (panel.isOpen()) {
				panel.close();
			} else {
				panel.open();
			}
		});
	}

	public static MenuItem config(ImIcon icon, String label, VideoConfigPanel panel) {
		return item(icon, label, panel.isOpen(), g -> {
			if (panel.isOpen()) {
				panel.close();
			} else {
				panel.open();
			}
		});
	}

	public static MenuItem menu(ImIcon icon, String label, Function<ImGraphics, List<MenuItem>> subItems) {
		return new MenuItem(icon, ImText.of(label), ImText.EMPTY, null, FLAG_MENU, null, subItems);
	}

	public static MenuItem menu(ImIcon icon, String label, BiConsumer<ImGraphics, List<MenuItem>> subItems) {
		return new MenuItem(icon, ImText.of(label), ImText.EMPTY, null, FLAG_MENU, null, graphics -> {
			var list = new ArrayList<MenuItem>();
			subItems.accept(graphics, list);
			return list;
		});
	}

	public static MenuItem menu(ImIcon icon, String label, LevelOfDetailValue lod) {
		return menu(icon, label, (graphics, menuItems) -> {
			if (lod.canBeAlways()) {
				menuItems.add(item(ImIcons.VISIBLE, "Always", lod.getType() == LevelOfDetailValue.Type.ALWAYS, g -> lod.setAlwaysVisible()).remainOpen(true));
				menuItems.add(item(ImIcons.INVISIBLE, "Never", lod.getType() == LevelOfDetailValue.Type.NEVER, g -> lod.setNeverVisible()).remainOpen(true));
			}
			menuItems.add(item(ImIcons.NUMBERS, "Within Distance", lod.getType() == LevelOfDetailValue.Type.WITHIN_DISTANCE, g -> lod.setVisibleWithin()).remainOpen(true));

			if (lod.getType() == LevelOfDetailValue.Type.WITHIN_DISTANCE) {
				menuItems.add(custom(g -> {
					ImGuiUtils.FLOAT.set((float) lod.getDistance());

					ImGui.setNextItemWidth(-1F);

					if (ImGui.dragFloat("###distance", ImGuiUtils.FLOAT.getData(), 1F, 0F, 256F)) {
						lod.setDistance(ImGuiUtils.FLOAT.get());
					}
				}));
			}
		});
	}

	public static MenuItem root(BiConsumer<ImGraphics, List<MenuItem>> subItems) {
		return menu(ImIcon.NONE, "Root", subItems);
	}

	public static MenuItem custom(OnClick imgui) {
		return new MenuItem(ImIcon.NONE, ImText.EMPTY, ImText.EMPTY, null, FLAG_CUSTOM_IMGUI, imgui, null);
	}

	public MenuItem withFlags(int add) {
		return new MenuItem(icon, label, tooltip, shortcut, flags | add, onClick, subItems);
	}

	public boolean hasFlag(int flag) {
		return (flags & flag) != 0;
	}

	public MenuItem remainOpen(boolean remainOpen) {
		return withFlags(FLAG_REMAIN_OPEN_OVERRIDE | (remainOpen ? FLAG_REMAIN_OPEN : 0));
	}

	public MenuItem withLabel(ImText label) {
		return new MenuItem(icon, label, tooltip, shortcut, flags, onClick, subItems);
	}

	public MenuItem withColor(@Nullable Color color) {
		return withLabel(label.withColor(color));
	}

	public MenuItem withShortcut(String shortcut) {
		return new MenuItem(icon, label, tooltip, shortcut, flags, onClick, subItems);
	}

	public MenuItem withTooltip(@Nullable ImText tooltip) {
		return new MenuItem(icon, label, tooltip, shortcut, flags, onClick, subItems);
	}

	public MenuItem withTooltip(String tooltip) {
		return withTooltip(ImText.of(tooltip));
	}

	public MenuItem disabled(boolean disabled) {
		return disabled ? withFlags(FLAG_DISABLED) : this;
	}

	public MenuItem enabled(boolean enabled) {
		return disabled(!enabled);
	}

	public void build(ImGraphics graphics) {
		if (hasFlag(FLAG_SKIP)) {
			return;
		} else if (hasFlag(FLAG_SEPARATOR)) {
			ImGui.separator();
			return;
		} else if (hasFlag(FLAG_CUSTOM_IMGUI)) {
			onClick.onClick(graphics);
			return;
		}

		var rIcon = icon == null ? ImIcon.NONE : icon;

		if (subItems != null) {
			var items = subItems.apply(graphics);

			if (!items.isEmpty()) {
				boolean remainOpen = hasFlag(FLAG_REMAIN_OPEN_OVERRIDE);

				if (remainOpen) {
					graphics.pushStack();
					graphics.setItemFlag(ImGuiItemFlags.SelectableDontClosePopup, hasFlag(FLAG_REMAIN_OPEN));
				}

				label.push(graphics);
				boolean menuOpen = ImGui.beginMenu(rIcon.formatLabel(graphics, label.text()), !hasFlag(FLAG_DISABLED));
				label.pop(graphics);

				if (menuOpen) {
					for (int i = 0; i < items.size(); i++) {
						ImGui.pushID(i);
						items.get(i).build(graphics);
						ImGui.popID();
					}

					ImGui.endMenu();
				}

				ImGuiUtils.hoveredTooltip(tooltip.text());

				if (remainOpen) {
					graphics.popStack();
				}
			} else {
				label.push(graphics);
				ImGui.beginMenu(rIcon.formatLabel(graphics, label.text()), false);
				label.pop(graphics);
			}
		} else if (onClick != null) {
			boolean remainOpen = hasFlag(FLAG_REMAIN_OPEN);

			if (remainOpen) {
				graphics.pushStack();
				graphics.setItemFlag(ImGuiItemFlags.SelectableDontClosePopup, true);
			}

			label.push(graphics);
			boolean menuItemClicked = ImGui.menuItem(rIcon.formatLabel(graphics, label.text()), shortcut, hasFlag(FLAG_CHECKMARK), !hasFlag(FLAG_DISABLED));
			label.pop(graphics);

			if (menuItemClicked && !hasFlag(FLAG_DISABLED)) {
				onClick.onClick(graphics);
			}

			ImGuiUtils.hoveredTooltip(tooltip.text());

			if (remainOpen) {
				graphics.popStack();
			}
		} else {
			label.push(graphics);
			ImGui.text(rIcon.formatLabel(graphics, label.text()));
			label.pop(graphics);

			ImGuiUtils.hoveredTooltip(tooltip.text());
		}
	}

	public void buildContextMenu(ImGraphics graphics) {
		if (subItems == null) {
			return;
		}

		var mainMenu = subItems.apply(graphics);

		if (!mainMenu.isEmpty()) {
			for (int i = 0; i < mainMenu.size(); i++) {
				ImGui.pushID(i);
				mainMenu.get(i).build(graphics);
				ImGui.popID();
			}
		}
	}

	public boolean buildMenuBar(ImGraphics graphics, boolean mainMenuBar) {
		if (subItems == null) {
			return false;
		}

		var mainMenu = subItems.apply(graphics);

		if (mainMenu.isEmpty()) {
			return false;
		}

		if (graphics.isReplay ? ImGui.beginMenu("VidLib") : mainMenuBar || ImGui.beginMenuBar()) {
			for (int i = 0; i < mainMenu.size(); i++) {
				ImGui.pushID(i);
				mainMenu.get(i).build(graphics);
				ImGui.popID();
			}

			if (graphics.isReplay) {
				ImGui.endMenu();
			} else if (!mainMenuBar) {
				ImGui.endMenuBar();
			}
		}

		return true;
	}
}
