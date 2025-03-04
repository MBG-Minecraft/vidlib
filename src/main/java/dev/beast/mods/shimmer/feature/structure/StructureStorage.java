package dev.beast.mods.shimmer.feature.structure;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.ShimmerConfig;
import dev.beast.mods.shimmer.util.Lazy;
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
	public static final StructureStorage SERVER = new StructureStorage();

	private record StructureSupplier(ResourceLocation id, Resource resource) implements Supplier<StructureTemplate> {
		@Override
		public StructureTemplate get() {
			try (var in = resource.open()) {
				var template = new StructureTemplate();
				template.load(BuiltInRegistries.BLOCK.asLookup(), NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()));
				return template;
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while loading structure " + id, ex);
				return null;
			}
		}
	}

	public final Map<ResourceLocation, Lazy<StructureTemplate>> structures;

	public StructureStorage() {
		this.structures = new HashMap<>();
	}

	@Nullable
	public StructureTemplate get(ResourceLocation id) {
		var lazy = structures.get(id);
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
		structures.clear();

		for (var entry : map.entrySet()) {
			structures.put(entry.getKey(), Lazy.of(new StructureSupplier(entry.getKey(), entry.getValue())));
		}
	}
}
