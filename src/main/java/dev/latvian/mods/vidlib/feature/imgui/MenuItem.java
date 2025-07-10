package dev.latvian.mods.vidlib.feature.imgui;

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
	public static final int FLAG_REMAIN_OPEN = 1 << 4;
	public static final int FLAG_SKIP = 1 << 5;

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

	public static MenuItem root(BiConsumer<ImGraphics, List<MenuItem>> subItems) {
		return menu(ImIcon.NONE, "Root", subItems);
	}

	public MenuItem withFlags(int add) {
		return new MenuItem(icon, label, tooltip, shortcut, flags | add, onClick, subItems);
	}

	public boolean hasFlag(int flag) {
		return (flags & flag) != 0;
	}

	public MenuItem remainOpen() {
		return withFlags(FLAG_REMAIN_OPEN);
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
		}

		var rIcon = icon == null ? ImIcon.NONE : icon;

		if (subItems != null) {
			var items = subItems.apply(graphics);

			if (!items.isEmpty()) {
				boolean remainOpen = hasFlag(FLAG_REMAIN_OPEN);

				if (remainOpen) {
					graphics.pushStack();
					graphics.setItemFlag(ImGuiItemFlags.SelectableDontClosePopup, true);
				}

				label.push(graphics);

				if (ImGui.beginMenu(rIcon.formatLabel(graphics, label.text()))) {
					label.pop(graphics);

					for (int i = 0; i < items.size(); i++) {
						ImGui.pushID(i);
						items.get(i).build(graphics);
						ImGui.popID();
					}

					ImGui.endMenu();
				}

				if (!tooltip.text().isEmpty() && ImGui.isItemHovered()) {
					ImGui.setTooltip(tooltip.text());
				}

				if (remainOpen) {
					graphics.popStack();
				}
			}
		} else if (onClick != null) {
			boolean remainOpen = hasFlag(FLAG_REMAIN_OPEN);

			if (remainOpen) {
				graphics.pushStack();
				graphics.setItemFlag(ImGuiItemFlags.SelectableDontClosePopup, true);
			}

			label.push(graphics);

			if (ImGui.menuItem(rIcon.formatLabel(graphics, label.text()), shortcut, hasFlag(FLAG_CHECKMARK), !hasFlag(FLAG_DISABLED))) {
				label.pop(graphics);

				if (!hasFlag(FLAG_DISABLED)) {
					onClick.onClick(graphics);
				}
			}

			if (!tooltip.text().isEmpty() && ImGui.isItemHovered()) {
				ImGui.setTooltip(tooltip.text());
			}

			if (remainOpen) {
				graphics.popStack();
			}
		} else {
			label.push(graphics);
			ImGui.text(rIcon.formatLabel(graphics, label.text()));
			label.pop(graphics);

			if (!tooltip.text().isEmpty() && ImGui.isItemHovered()) {
				ImGui.setTooltip(tooltip.text());
			}
		}
	}

	public void buildRoot(ImGraphics graphics, boolean mainMenuBar) {
		if (subItems == null) {
			return;
		}

		var mainMenu = subItems.apply(graphics);

		if (!mainMenu.isEmpty()) {
			if (mainMenuBar ? ImGui.beginMainMenuBar() : ImGui.beginMenuBar()) {
				for (int i = 0; i < mainMenu.size(); i++) {
					ImGui.pushID(i);
					mainMenu.get(i).build(graphics);
					ImGui.popID();
				}

				if (mainMenuBar) {
					ImGui.endMainMenuBar();
				} else {
					ImGui.endMenuBar();
				}
			}
		}
	}
}
