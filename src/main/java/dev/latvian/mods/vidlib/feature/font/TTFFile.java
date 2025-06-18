package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;

public record TTFFile(ResourceLocation id, byte[] bytes) {
	public static final VLRegistry<TTFFile> REGISTRY = VLRegistry.createClient("ttf", TTFFile.class);
	public static final RegistryRef<TTFFile> COUSINE_REGULAR = REGISTRY.ref(VidLib.id("cousine_regular"));
	public static final RegistryRef<TTFFile> OPEN_SANS_REGULAR = REGISTRY.ref(VidLib.id("opensans_regular"));
	public static final RegistryRef<TTFFile> JETBRAINS_MONO_REGULAR = REGISTRY.ref(VidLib.id("jetbrainsmono_regular"));

	public static void load(ResourceManager resourceManager) {
		var map = new HashMap<ResourceLocation, TTFFile>();

		for (var entry : resourceManager.listResources("vidlib/ttf", id -> !id.getPath().startsWith("_") && (id.getPath().endsWith(".ttf") || id.getPath().endsWith(".otf"))).entrySet()) {
			var id = entry.getKey().withPath(s -> s.substring(11, s.length() - 4));

			try (var in = entry.getValue().open()) {
				map.put(id, new TTFFile(id, in.readAllBytes()));
			} catch (Exception e) {
				throw new RuntimeException("Failed to read TTF file: " + id, e);
			}
		}

		REGISTRY.update(map);
	}
}
