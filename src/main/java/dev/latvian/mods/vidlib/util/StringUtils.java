package dev.latvian.mods.vidlib.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

public interface StringUtils {
	Set<String> ALWAYS_LOWER_CASE = new HashSet<>(Arrays.asList("a", "an", "the", "of", "on", "in", "and", "or", "but", "for"));

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
}
