package dev.beast.mods.shimmer.feature.structure;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.util.WithCache;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

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
				if (id.getPath().endsWith(".nbt")) {
					var template = new StructureTemplate();
					template.load(BuiltInRegistries.BLOCK, NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap()));
					structures = StructureHolder.allOf(template);
				} else if (id.getPath().endsWith(".vstruct")) {
					try (var stream = new GZIPInputStream(in)) {
						var buf = Unpooled.wrappedBuffer(stream.readAllBytes());
						var struct = StructureHolder.fromVStruct(buf);
						structures = List.of(struct);
					}
				}
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while loading structure " + id, ex);
			}
		}

		return structures;
	}

	@Override
	public void clearCache() {
		structures = null;
	}
}
