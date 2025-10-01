package dev.latvian.mods.vidlib.feature.imgui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public interface ImGuiAPI {
	List<BooleanSupplier> HIDE = new ArrayList<>(1);

	static boolean getHide() {
		for (var s : HIDE) {
			if (s.getAsBoolean()) {
				return true;
			}
		}

		return false;
	}
}
