package dev.beast.mods.shimmer.feature.gradient;

import com.mojang.blaze3d.platform.NativeImage;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.util.registry.RegistryRef;
import dev.beast.mods.shimmer.util.registry.ShimmerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class ClientGradients extends SimplePreparableReloadListener<Map<ResourceLocation, Gradient>> {
	public static final ShimmerRegistry<Gradient> REGISTRY = ShimmerRegistry.createClient("gradient", true);

	public static final RegistryRef<Gradient> FIRE_1 = REGISTRY.ref(Shimmer.id("fire/1"));
	public static final RegistryRef<Gradient> FIRE_2 = REGISTRY.ref(Shimmer.id("fire/2"));
	public static final RegistryRef<Gradient> FIRE_3 = REGISTRY.ref(Shimmer.id("fire/3"));
	public static final RegistryRef<Gradient> FIRE_4 = REGISTRY.ref(Shimmer.id("fire/4"));
	public static final RegistryRef<Gradient> SPARK = REGISTRY.ref(Shimmer.id("spark"));

	@Override
	protected Map<ResourceLocation, Gradient> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, Gradient>();

		for (var entry : resourceManager.listResources("textures/shimmer/gradient", id -> !id.getPath().startsWith("_") && id.getPath().endsWith(".png")).entrySet()) {
			try (var in = entry.getValue().open()) {
				var id = entry.getKey().withPath(s -> s.substring(26, s.length() - 4));

				try (var image = NativeImage.read(in)) {
					var pixels = new Color[image.getWidth()];

					for (int i = 0; i < pixels.length; i++) {
						pixels[i] = Color.of(image.getPixel(i, 0));
					}

					map.put(id, new PixelGradient(pixels));
				}
			} catch (Exception ex) {
				Shimmer.LOGGER.error("Error while reading file " + entry.getKey(), ex);
			}
		}

		return map;
	}

	@Override
	protected void apply(Map<ResourceLocation, Gradient> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		REGISTRY.update(Map.copyOf(from));
	}
}
