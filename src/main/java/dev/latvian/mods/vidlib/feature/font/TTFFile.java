package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;

public record TTFFile(ResourceLocation id, ResourceLocation resource) {
	public static final VLRegistry<TTFFile> REGISTRY = VLRegistry.createClient("ttf", TTFFile.class);
	public static final RegistryRef<TTFFile> MATERIAL_ICONS_ROUND_REGULAR = REGISTRY.ref(VidLib.id("materialiconsround_regular"));
	public static final RegistryRef<TTFFile> JETBRAINS_MONO_REGULAR = REGISTRY.ref(VidLib.id("jetbrainsmono_regular"));

	public static void find(ResourceManager resourceManager) {
		var map = new HashMap<ResourceLocation, TTFFile>();

		for (var entry : resourceManager.listResources("vidlib/ttf", id -> !id.getPath().startsWith("_") && (id.getPath().endsWith(".ttf") || id.getPath().endsWith(".otf"))).entrySet()) {
			var id = entry.getKey().withPath(s -> s.substring(11, s.length() - 4));
			map.put(id, new TTFFile(id, entry.getKey()));
		}

		REGISTRY.update(map);
	}

	public byte[] load(ResourceManager resourceManager) throws IOException {
		try (var in = resourceManager.getResource(resource).orElseThrow().open()) {
			return in.readAllBytes();
		} catch (Exception e) {
			throw new RuntimeException("Failed to read TTF file: " + id, e);
		}
	}
}
