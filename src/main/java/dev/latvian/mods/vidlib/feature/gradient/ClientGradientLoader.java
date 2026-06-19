package dev.latvian.mods.vidlib.feature.gradient;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.CompoundGradient;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.color.GradientReference;
import dev.latvian.mods.vidlib.VidLib;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientGradientLoader extends SimplePreparableReloadListener<Map<Identifier, Gradient>> {
	@Override
	protected Map<Identifier, Gradient> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<Identifier, Gradient>();

		for (var entry : resourceManager.listResources("textures/vidlib/gradient", id -> !id.getPath().startsWith("_") && id.getPath().endsWith(".png")).entrySet()) {
			try (var in = entry.getValue().open()) {
				var id = entry.getKey().withPath(s -> s.substring(25, s.length() - 4));

				try (var image = NativeImage.read(in)) {
					var pixels = new ArrayList<Color>(image.getWidth());

					for (int x = 0; x < image.getWidth(); x++) {
						pixels.add(Color.of(image.getPixel(x, 0)));
					}

					map.put(id, CompoundGradient.ofColors(pixels));
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while reading file " + entry.getKey(), ex);
			}
		}

		return map;
	}

	@Override
	protected void apply(Map<Identifier, Gradient> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		GradientReference.MAP = Map.copyOf(from);
	}
}
