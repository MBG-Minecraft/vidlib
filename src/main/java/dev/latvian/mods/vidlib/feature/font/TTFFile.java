package dev.latvian.mods.vidlib.feature.font;

import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;

public record TTFFile(ResourceLocation id, ResourceLocation resource) {
	public static final VLRegistry<TTFFile> REGISTRY = VLRegistry.createClient("ttf", TTFFile.class);
	public static final RegistryRef<TTFFile> MATERIAL_ICONS_ROUND_REGULAR = REGISTRY.ref(ResourceLocation.tryBuild("imguiresources", "materialiconsround_regular"));
	public static final RegistryRef<TTFFile> JETBRAINS_MONO_REGULAR = REGISTRY.ref(ResourceLocation.tryBuild("imguiresources", "jetbrainsmono_regular"));

	public static void find(ResourceManager resourceManager) {
		var map = new HashMap<ResourceLocation, TTFFile>();
		map.put(MATERIAL_ICONS_ROUND_REGULAR.id(), new TTFFile(MATERIAL_ICONS_ROUND_REGULAR.id()));
		map.put(JETBRAINS_MONO_REGULAR.id(), new TTFFile(JETBRAINS_MONO_REGULAR.id()));
		REGISTRY.update(map);
	}

	public TTFFile(ResourceLocation id) {
		this(id, id.withPath(p -> p + ".ttf"));
	}

	public byte[] load(ResourceManager resourceManager) throws IOException {
		try (var in = resourceManager.getResource(resource).orElseThrow().open()) {
			return in.readAllBytes();
		} catch (Exception e) {
			throw new RuntimeException("Failed to read TTF file: " + id, e);
		}
	}
}
