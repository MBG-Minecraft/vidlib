package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.klib.CommonPaths;
import dev.latvian.mods.vidlib.VidLib;
import imgui.ImGui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ImGuiIni {
	private static final Map<Long, String> PATHS = new HashMap<>();
	private static final Map<Long, String> WRITTEN = new HashMap<>();
	private static final Set<Long> RESTORED = new HashSet<>();
	private static final Set<Long> LOGGED_FAILURES = new HashSet<>();

	private ImGuiIni() {
	}

	public static void tick(boolean inGame) {
		long key = 0L;

		try {
			var context = ImGui.getCurrentContext();

			if (context == null || context.ptr == 0) {
				return;
			}

			key = context.ptr;
			var io = ImGui.getIO();
			String filename = io.getIniFilename();

			if (filename != null && !filename.isEmpty()) {
				PATHS.put(key, filename);
				io.setIniFilename("");
			}

			String file = PATHS.get(key);
			if (file == null) {
				return;
			}

			Path path1 = Path.of(file);
			if (inGame) {
				Path path = path1;
				restore(key, path);
			}

			String ini = ImGui.saveIniSettingsToMemory();
			if (ini == null) {
				return;
			}

			String current = stripClosed(ini);

			if (!current.equals(WRITTEN.get(key))) {
				Files.writeString(CommonPaths.mkdirs(path1), current);
				WRITTEN.put(key, current);
			}
		} catch (Exception ex) {
			if (key != 0L && LOGGED_FAILURES.add(key)) {
				VidLib.LOGGER.error("Failed to save imgui settings", ex);
			}
		}
	}

	private static void restore(long key, Path path) {
		if (!RESTORED.add(key)) {
			return;
		}

		try {
			if (!Files.isRegularFile(path)) {
				return;
			}

			Set<String> openWindows = new HashSet<>();

			for (var line : Files.readAllLines(path)) {
				String trimmed = line.trim();

				if (trimmed.startsWith("[Window][") && trimmed.endsWith("]")) {
					openWindows.add(trimmed.substring("[Window][".length(), trimmed.length() - 1));
				}
			}

			int count = 0;

			for (var panel : Panel.REGISTRY.values()) {
				if (panel.rememberOpen && !panel.isOpen() && containsAny(openWindows, panel)) {
					panel.open();
					count++;
				}
			}

			if (count > 0) {
				VidLib.LOGGER.info("Restored " + count + " open panel(s) from " + path.toAbsolutePath());
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to restore open panels from " + path, ex);
		}
	}

	private static boolean containsAny(Set<String> openWindows, Panel panel) {
		for (String name : panel.iniSectionNames()) {
			if (openWindows.contains(name)) {
				return true;
			}
		}

		return false;
	}

	private static String stripClosed(String ini) {
		Set<String> strip = new HashSet<>();

		for (var panel : Panel.REGISTRY.values()) {
			if (panel.rememberOpen && !panel.isOpen()) {
				strip.addAll(panel.iniSectionNames());
			}
		}

		if (strip.isEmpty()) {
			return ini;
		}

		String[] lines = ini.split("\n", -1);
		List<String> kept = new ArrayList<>(lines.length);

		for (int i = 0; i < lines.length; i++) {
			String trimmed = lines[i].trim();

			if (trimmed.startsWith("[Window][") && trimmed.endsWith("]")) {
				String name = trimmed.substring("[Window][".length(), trimmed.length() - 1);

				if (strip.contains(name)) {
					i++;

					while (i < lines.length && !lines[i].trim().startsWith("[")) {
						i++;
					}

					i--;
					continue;
				}
			}

			kept.add(lines[i]);
		}

		return String.join("\n", kept);
	}
}
