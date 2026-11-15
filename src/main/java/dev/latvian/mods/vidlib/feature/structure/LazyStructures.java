package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.klib.util.WithCache;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class LazyStructures implements WithCache, Supplier<List<StructureHolder>> {
	public final Identifier id;
	@Nullable
	public final Path path;
	@Nullable
	public final Resource resource;
	private List<StructureHolder> structures;

	public LazyStructures(Identifier id, Resource resource) {
		this(id, null, resource);
	}

	public LazyStructures(Identifier id, @Nullable Path path, @Nullable Resource resource) {
		this.id = id;
		this.path = path;
		this.resource = resource;
	}

	@Override
	public List<StructureHolder> get() {
		if (structures == null) {
			structures = List.of();

			try (var in = path != null ? Files.newInputStream(path) : resource.open()) {
				structures = StructureIO.read(id, in);
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