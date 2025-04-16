package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.util.WithCache;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.function.Supplier;

public class LazyStructures implements WithCache, Supplier<List<StructureHolder>> {
	public final ResourceLocation id;
	public final Resource resource;
	private List<StructureHolder> structures;

	public LazyStructures(ResourceLocation id, Resource resource) {
		this.id = id;
		this.resource = resource;
	}

	@Override
	public List<StructureHolder> get() {
		if (structures == null) {
			structures = List.of();

			try (var in = resource.open()) {
				var template = new StructureTemplate();
				template.load(BuiltInRegistries.BLOCK, NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()));
				structures = StructureHolder.allOf(template);
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while loading structure " + id, ex);
			}
		}

		return structures;
	}

	@Override
	public void clearCache() {
		structures = null;
	}
}
