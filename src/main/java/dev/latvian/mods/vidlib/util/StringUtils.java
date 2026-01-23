package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.util.Empty;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TimeZone;

public interface StringUtils {
	Set<String> ALWAYS_LOWER_CASE = new HashSet<>(Arrays.asList("a", "an", "the", "of", "on", "in", "and", "or", "but", "for"));

	SimpleDateFormat TIMESTAMP_FORMAT = Util.make(() -> {
		var format = new SimpleDateFormat("HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("EST"));
		return format;
	});

	static String snakeCaseToTitleCase(String string) {
		StringJoiner joiner = new StringJoiner(" ");
		String[] split = string.split("_");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			String titleCase = toTitleCase(s, i == 0);
			joiner.add(titleCase);
		}
		return joiner.toString();
	}

	static String toTitleCase(String s) {
		return toTitleCase(s, false);
	}

	static String toTitleCase(String s, boolean ignoreSpecial) {
		if (s.isEmpty()) {
			return "";
		} else if (!ignoreSpecial && ALWAYS_LOWER_CASE.contains(s)) {
			return s;
		} else if (s.length() == 1) {
			return s.toUpperCase(Locale.ROOT);
		}

		char[] chars = s.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	static String timer(long millis) {
		return "%02d:%02d:%03d".formatted(millis / 60000L, (millis / 1000L) % 60, millis % 1000L);
	}

	static String clock(long seconds) {
		return "%02d:%02d:%02d ".formatted(seconds / 3600L, (seconds / 60L) % 60L, seconds % 60L);
	}

	@Nullable
	static Component buildComponent(@Nullable Component prefix, @Nullable Component original, @Nullable Component suffix) {
		if (Empty.isEmpty(prefix)) {
			prefix = null;
		}

		if (Empty.isEmpty(suffix)) {
			suffix = null;
		}

		if (prefix == null && suffix == null) {
			return original;
		}

		if (Empty.isEmpty(original)) {
			original = null;
		}

		var component = Component.empty();

		if (prefix != null) {
			component.append(prefix);
		}

		if (original != null) {
			component.append(original);
		}

		if (suffix != null) {
			component.append(suffix);
		}

		return component;
	}

	static String normalizeFileName(String name) {
		name = name.trim();
		int index = name.lastIndexOf('.');

		if (index != -1) {
			name = name.substring(0, index);
		}

		name = name.replaceAll("[^-\\w]", "_").replaceAll("_{2,}", "_");

		if (name.startsWith("_")) {
			name = name.substring(1);
		}

		if (name.endsWith("_")) {
			name = name.substring(0, name.length() - 1);
		}

		return name.isBlank() ? "" : name;
	}
}
