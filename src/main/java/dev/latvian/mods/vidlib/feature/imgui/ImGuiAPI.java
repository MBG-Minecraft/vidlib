package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.replay.api.ReplayAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public interface ImGuiAPI {
	List<BooleanSupplier> HIDE = new ArrayList<>(1);

	static boolean getHide() {
		if (ReplayAPI.getActive().isExporting()) {
			return true;
		}

		for (var s : HIDE) {
			if (s.getAsBoolean()) {
				return true;
			}
		}

		return false;
	}
}
