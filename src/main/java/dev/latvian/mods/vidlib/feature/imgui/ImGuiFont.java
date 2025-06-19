package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.font.TTFFile;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ImGuiFont implements Supplier<ImFont> {
	public static final List<ImGuiFont> ALL = new ArrayList<>();

	public static void register(ImGuiFont font) {
		ALL.add(font);
	}

	public final RegistryRef<TTFFile> fontFile;
	public final int size;
	private ImFont font;

	public ImGuiFont(RegistryRef<TTFFile> fontFile, int size) {
		this.fontFile = fontFile;
		this.size = size;
	}

	public void loadFont(ImFontAtlas fonts, ResourceManager resourceManager, ImFontConfig config) {
		try {
			var bytes = fontFile.get().load(resourceManager);
			config.setName(fontFile.id().toString() + "/" + size);
			font = Objects.requireNonNull(fonts.addFontFromMemoryTTF(bytes, size, config));
		} catch (
			Exception ex) {
			throw new RuntimeException("Failed to load font: " + fontFile.id().toString() + "/" + size, ex);
		}
	}

	@Override
	public ImFont get() {
		return Objects.requireNonNull(font, "Font not loaded");
	}
}
