package dev.beast.mods.shimmer.feature.structure;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class ClientStructureStorage extends StructureStorage {
	public static final ClientStructureStorage CLIENT = new ClientStructureStorage();

	private final Map<ResourceLocation, StructureRenderer> renderers = new HashMap<>();

	public ClientStructureStorage() {
		super(false);
	}

	public StructureRenderer getRenderer(ResourceLocation id) {
		return renderers.computeIfAbsent(id, k -> new StructureRenderer(this, k));
	}

	@Override
	protected void apply(Map<ResourceLocation, Resource> map, ResourceManager resourceManager, ProfilerFiller profiler) {
		for (var renderer : renderers.values()) {
			renderer.close();
		}

		super.apply(map, resourceManager, profiler);
	}
}
