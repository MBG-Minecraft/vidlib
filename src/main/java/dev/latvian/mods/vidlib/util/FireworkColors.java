package dev.latvian.mods.vidlib.util;

import dev.latvian.mods.klib.color.Color;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

public interface FireworkColors {
	IntList NONE = IntLists.emptyList();
	IntList RGB = IntArrayList.of(Color.RED.rgb(), Color.GREEN.rgb(), Color.BLUE.rgb());
	IntList CMY = IntArrayList.of(Color.CYAN.rgb(), Color.MAGENTA.rgb(), Color.YELLOW.rgb());
	IntList SUCCESS = IntArrayList.of(Color.WHITE.rgb(), Color.YELLOW.rgb());
	IntList FAIL_START = IntArrayList.of(Color.RED.rgb(), 0x330000);
	IntList FAIL_END = IntArrayList.of(0x330000);
}
