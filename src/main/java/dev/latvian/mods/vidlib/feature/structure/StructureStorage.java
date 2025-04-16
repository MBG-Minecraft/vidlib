package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructureStorage extends SimplePreparableReloadListener<Map<ResourceLocation, Resource>> {
	public static final VLRegistry<LazyStructures> SERVER = VLRegistry.createServer("server_structure");
	public static final VLRegistry<LazyStructures> CLIENT = VLRegistry.createClient("client_structure");

	public final VLRegistry<LazyStructures> registry;

	public StructureStorage(VLRegistry<LazyStructures> registry) {
		this.registry = registry;
	}

	@Nullable
	public List<StructureHolder> get(ResourceLocation id) {
		var lazy = registry.get(id);
		return lazy != null ? lazy.get() : List.of();
	}

	@Override
	protected Map<ResourceLocation, Resource> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, Resource>();

		for (var entry : resourceManager.listResources("structure", p -> p.getPath().endsWith(".nbt")).entrySet()) {
			map.put(entry.getKey().withPath(p -> p.substring(10, p.length() - 4)), entry.getValue());
		}

		return map;
	}

	@Override
	protected void apply(Map<ResourceLocation, Resource> map, ResourceManager resourceManager, ProfilerFiller profiler) {
		var map2 = new HashMap<ResourceLocation, LazyStructures>();

		for (var entry : map.entrySet()) {
			map2.put(entry.getKey(), new LazyStructures(entry.getKey(), entry.getValue()));
		}

		registry.update(map2);
		(registry.getSide().isServer() ? AutoInit.Type.SERVER_STRUCTURES_LOADED : AutoInit.Type.CLIENT_STRUCTURES_LOADED).invoke(this);
	}
}
