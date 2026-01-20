package dev.latvian.mods.vidlib.feature.dynamicresources;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DynamicPackResources extends AbstractPackResources {
	private final Map<ResourceLocation, IoSupplier<InputStream>> resources;
	private final Set<String> namespaces;

	public DynamicPackResources(Map<ResourceLocation, IoSupplier<InputStream>> resources) {
		super(new PackLocationInfo("vidlib:dynamic_resources", Component.literal("VidLib Dynamic Resources"), PackSource.BUILT_IN, Optional.empty()));
		this.resources = new Object2ObjectOpenHashMap<>(resources);
		this.namespaces = new HashSet<>();

		for (var entry : resources.entrySet()) {
			this.namespaces.add(entry.getKey().getNamespace());
		}
	}

	@Override
	@Nullable
	public IoSupplier<InputStream> getRootResource(String... elements) {
		return null;
	}

	@Override
	@Nullable
	public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
		return resources.get(location);
	}

	@Override
	public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
		for (var entry : resources.entrySet()) {
			if (entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(path)) {
				resourceOutput.accept(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return namespaces;
	}

	@Override
	public void close() {
		resources.clear();
	}
}
