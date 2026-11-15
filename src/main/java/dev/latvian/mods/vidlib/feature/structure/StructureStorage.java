package dev.latvian.mods.vidlib.feature.structure;

import dev.latvian.mods.klib.registry.RefType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StructureStorage extends SimplePreparableReloadListener<Map<Identifier, Resource>> {
	public static final Map<String, LazyStructures> SERVER = new ConcurrentHashMap<>();
	public static final Map<String, LazyStructures> CLIENT = new ConcurrentHashMap<>();

	private static final String ROOT = "structure/";
	private static final String[] EXTENSIONS = new String[]{".nbt", ".vstruct", ".schem"};
	private static final Map<Identifier, LazyStructures> LOCAL_CACHE = new ConcurrentHashMap<>();

	public final RefType refType;
	public final Map<String, LazyStructures> registry;

	public StructureStorage(RefType refType) {
		this.refType = refType;
		this.registry = refType == RefType.SERVER ? SERVER : CLIENT;
	}

	@Nullable
	public static LazyStructures findLocal(Identifier id) {
		if (!"video".equals(id.getNamespace())) {
			return null;
		}

		var base = VidLibPaths.GAME.get().resolve(id.getPath());

		for (var extension : EXTENSIONS) {
			var path = base.resolveSibling(base.getFileName().toString() + extension);

			if (Files.isRegularFile(path)) {
				return LOCAL_CACHE.computeIfAbsent(id, ignored -> new LazyStructures(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + extension), path, null));
			}
		}

		return null;
	}

	@Override
	protected Map<Identifier, Resource> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<Identifier, Resource>();

		for (var extension : EXTENSIONS) {
			for (var entry : resourceManager.listResources("structure", id -> id.getPath().endsWith(extension)).entrySet()) {
				map.put(entry.getKey().withPath(path -> path.substring(ROOT.length())), entry.getValue());
			}
		}

		return map;
	}

	@Override
	protected void apply(Map<Identifier, Resource> map, ResourceManager resourceManager, ProfilerFiller profiler) {
		var map2 = new HashMap<String, LazyStructures>(map.size());

		for (var entry : map.entrySet()) {
			var key = entry.getKey().withPath(path -> path.substring(0, path.lastIndexOf('.')));
			map2.put(key.toString(), new LazyStructures(entry.getKey(), entry.getValue()));
		}

		scanLocalFiles(map2);

		LOCAL_CACHE.clear();
		registry.clear();
		registry.putAll(map2);

		(refType == RefType.SERVER ? AutoInit.Type.SERVER_STRUCTURES_LOADED : AutoInit.Type.CLIENT_STRUCTURES_LOADED).invoke(this);
	}

	private void scanLocalFiles(Map<String, LazyStructures> map) {
		var root = VidLibPaths.GAME.get();

		if (!Files.isDirectory(root)) {
			return;
		}

		try (var stream = Files.walk(root)) {
			for (var path : stream.filter(Files::isRegularFile).toList()) {
				var rel = root.relativize(path).toString().replace('\\', '/');

				for (var extension : EXTENSIONS) {
					if (rel.endsWith(extension)) {
						var id = Identifier.fromNamespaceAndPath("video", rel.substring(0, rel.length() - extension.length()));
						map.put(id.toString(), new LazyStructures(Identifier.fromNamespaceAndPath("video", rel), path, null));
						break;
					}
				}
			}
		} catch (IOException ex) {
			VidLib.LOGGER.error("Failed to scan local structures in " + root, ex);
		}
	}
}