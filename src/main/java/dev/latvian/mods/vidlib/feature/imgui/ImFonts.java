package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.font.TTFFile;
import imgui.ImFont;

public interface ImFonts {
	ImGuiFont OPEN_SANS_19 = new ImGuiFont(TTFFile.OPEN_SANS_REGULAR, 19);

	ImGuiFont COUSINE_13 = new ImGuiFont(TTFFile.COUSINE_REGULAR, 13);
	ImGuiFont COUSINE_16 = new ImGuiFont(TTFFile.COUSINE_REGULAR, 16);
	ImGuiFont COUSINE_19 = new ImGuiFont(TTFFile.COUSINE_REGULAR, 19);
	ImGuiFont COUSINE_22 = new ImGuiFont(TTFFile.COUSINE_REGULAR, 22);
	ImGuiFont COUSINE_25 = new ImGuiFont(TTFFile.COUSINE_REGULAR, 25);

	ImGuiFont JETBRAINS_MONO_16 = new ImGuiFont(TTFFile.JETBRAINS_MONO_REGULAR, 16);
	ImGuiFont JETBRAINS_MONO_19 = new ImGuiFont(TTFFile.JETBRAINS_MONO_REGULAR, 19);

	static void bootstrap() {
		ImGuiFont.register(OPEN_SANS_19);
		ImGuiFont.register(COUSINE_13);
		ImGuiFont.register(COUSINE_16);
		ImGuiFont.register(COUSINE_19);
		ImGuiFont.register(COUSINE_22);
		ImGuiFont.register(COUSINE_25);
		ImGuiFont.register(JETBRAINS_MONO_16);
		ImGuiFont.register(JETBRAINS_MONO_19);
	}

	static ImFont getOpenSans19() {
		return OPEN_SANS_19.get();
	}

	static ImFont getCousine13() {
		return COUSINE_13.get();
	}

	static ImFont getCousine16() {
		return COUSINE_16.get();
	}

	static ImFont getCousine19() {
		return COUSINE_19.get();
	}

	static ImFont getCousine22() {
		return COUSINE_22.get();
	}

	static ImFont getCousine25() {
		return COUSINE_25.get();
	}

	static ImFont getJetbrainsMono16() {
		return JETBRAINS_MONO_16.get();
	}

	static ImFont getJetbrainsMono19() {
		return JETBRAINS_MONO_19.get();
	}
}
