package dev.beast.mods.shimmer.feature.structure;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.util.Lazy;
import dev.beast.mods.shimmer.util.registry.ShimmerRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StructureStorage extends SimplePreparableReloadListener<Map<ResourceLocation, Resource>> {
	public static final ShimmerRegistry<Lazy<StructureTemplate>> SERVER = ShimmerRegistry.createServer("server_structure", false);
	public static final ShimmerRegistry<Lazy<StructureTemplate>> CLIENT = ShimmerRegistry.createClient("client_structure", false);

	private record StructureSupplier(ResourceLocation id, Resource resource) implements Supplier<StructureTemplate> {
		@Override
		public StructureTemplate get() {
			try (var in = resource.open()) {
				var template = new StructureTemplate();
				template.load(BuiltInRegistries.BLOCK, NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()));
				return template;
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while loading structure " + id, ex);
				return null;
			}
		}
	}

	public final ShimmerRegistry<Lazy<StructureTemplate>> registry;

	public StructureStorage(ShimmerRegistry<Lazy<StructureTemplate>> registry) {
		this.registry = registry;
	}

	@Nullable
	public StructureTemplate get(ResourceLocation id) {
		var lazy = registry.get(id);
		return lazy != null ? lazy.get() : null;
	}

	@Override
	protected Map<ResourceLocation, Resource> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, Resource>();

		for (var entry : resourceManager.listResources("structure", p -> (ShimmerConfig.loadVanillaStructures || !p.getNamespace().equals("minecraft")) && p.getPath().endsWith(".nbt")).entrySet()) {
			map.put(entry.getKey().withPath(p -> p.substring(10, p.length() - 4)), entry.getValue());
		}

		return map;
	}

	@Override
	protected void apply(Map<ResourceLocation, Resource> map, ResourceManager resourceManager, ProfilerFiller profiler) {
		var map2 = new HashMap<ResourceLocation, Lazy<StructureTemplate>>();

		for (var entry : map.entrySet()) {
			map2.put(entry.getKey(), Lazy.of(new StructureSupplier(entry.getKey(), entry.getValue())));
		}

		registry.update(map2);

		(registry.getSide().isServer() ? AutoInit.Type.SERVER_STRUCTURES_LOADED : AutoInit.Type.CLIENT_STRUCTURES_LOADED).invoke(this);
	}
}
