package dev.latvian.mods.vidlib.feature.dynamicresources;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.util.ByteArrayIoSupplier;
import dev.latvian.mods.vidlib.util.JsonUtils;
import dev.latvian.mods.vidlib.util.StringIoSupplier;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DynamicResources(
	Map<ResourceLocation, Map<String, String>> resources,
	List<DynamicResourceFile> files,
	Optional<ResourceLocation> colorMap,
	List<DynamicResourceFile> textures
) {
	public static final Codec<DynamicResources> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(ResourceLocation.CODEC, Codec.unboundedMap(Codec.STRING, Codec.STRING)).fieldOf("resources").forGetter(DynamicResources::resources),
		DynamicResourceFile.CODEC.listOf().optionalFieldOf("files", List.of()).forGetter(DynamicResources::files),
		ResourceLocation.CODEC.optionalFieldOf("color_map").forGetter(DynamicResources::colorMap),
		DynamicResourceFile.CODEC.listOf().optionalFieldOf("textures", List.of()).forGetter(DynamicResources::textures)
	).apply(instance, DynamicResources::new));

	public static List<PackResources> injectPackResources(PackType packType, List<PackResources> packResourcesList) {
		VidLib.LOGGER.info("Loading Dynamic Resources...");
		var packResources = new Object2ObjectOpenHashMap<ResourceLocation, IoSupplier<InputStream>>();

		PlatformHelper.CURRENT.collectDynamicResources(packType, dynamicResourcesId -> {
			VidLib.LOGGER.info("Loading " + dynamicResourcesId);
			var path = PlatformHelper.CURRENT.findFile(packType, dynamicResourcesId.withSuffix(".json"));

			if (path != null) {
				try (var reader = Files.newBufferedReader(path)) {
					var json = JsonUtils.read(reader);
					var dynamicResources = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
					load(packType, packResources, dynamicResources);
				} catch (Exception ex) {
					throw new RuntimeException("Failed to load dynamic resources " + dynamicResourcesId, ex);
				}
			} else {
				throw new RuntimeException("Failed to load dynamic resources " + dynamicResourcesId + ": File not found");
			}
		});

		VidLib.LOGGER.info("Created " + packResources.size() + " Dynamic Resources");

		packResourcesList = new ArrayList<>(packResourcesList);
		packResourcesList.addLast(new DynamicPackResources(packResources));
		return List.copyOf(packResourcesList);
	}

	public static void load(PackType packType, Map<ResourceLocation, IoSupplier<InputStream>> packResources, DynamicResources dynamicResources) throws IOException {
		var resourcesList = List.copyOf(dynamicResources.resources.entrySet());

		if (!dynamicResources.files.isEmpty()) {
			for (var file : dynamicResources.files) {
				var inPath = PlatformHelper.CURRENT.findFile(packType, file.template());
				var template = Files.readString(inPath);

				for (var resource : resourcesList) {
					var id = resource.getKey();
					var outputPath = ResourceLocation.parse(file.location().replace("{namespace}", id.getNamespace()).replace("{path}", id.getPath()));
					var output = template.replace("{namespace}", id.getNamespace()).replace("{path}", id.getPath());

					for (var entry : resource.getValue().entrySet()) {
						output = output.replace("{" + entry.getKey() + "}", entry.getValue());
					}

					if (outputPath.getPath().endsWith(".json")) {
						output = JsonUtils.string(JsonUtils.GSON.fromJson(output, JsonElement.class));
					}

					packResources.put(outputPath, new StringIoSupplier(output));
				}
			}
		}

		if (!dynamicResources.textures.isEmpty()) {
			var remap = new Int2IntMap[resourcesList.size()];

			if (dynamicResources.colorMap.isPresent()) {
				var inPath = PlatformHelper.CURRENT.findFile(packType, dynamicResources.colorMap.get());

				try (var in = Files.newInputStream(inPath)) {
					var colorMap = ImageIO.read(in);
					int w = colorMap.getWidth();
					int h = colorMap.getHeight();

					var keys = new int[h];

					for (int y = 0; y < h; y++) {
						keys[y] = colorMap.getRGB(0, y);
					}

					for (int x = 1; x < w; x++) {
						if (x - 1 < remap.length) {
							var map = new Int2IntOpenHashMap();

							for (int y = 0; y < h; y++) {
								var col = colorMap.getRGB(x, y);

								if (keys[y] != col) {
									map.put(keys[y], col);
								}
							}

							if (!map.isEmpty()) {
								remap[x - 1] = map;
							}
						}
					}
				}
			}

			for (var file : dynamicResources.textures) {
				var inPath = PlatformHelper.CURRENT.findFile(packType, file.template());

				try (var in = Files.newInputStream(inPath)) {
					var template = ImageIO.read(in);
					int w = template.getWidth();
					int h = template.getHeight();

					for (int i = 0; i < resourcesList.size(); i++) {
						var resource = resourcesList.get(i);
						var id = resource.getKey();
						var outputPath = ResourceLocation.parse(file.location().replace("{namespace}", id.getNamespace()).replace("{path}", id.getPath()));

						if (remap[i] != null) {
							var img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

							for (int y = 0; y < h; y++) {
								for (int x = 0; x < w; x++) {
									int col = template.getRGB(x, y);
									img.setRGB(x, y, remap[i].getOrDefault(col, col));
								}
							}

							try (var out = new ByteArrayOutputStream()) {
								ImageIO.write(img, "png", out);
								packResources.put(outputPath, new ByteArrayIoSupplier(out.toByteArray()));
							}
						} else {
							try (var out = new ByteArrayOutputStream()) {
								ImageIO.write(template, "png", out);
								packResources.put(outputPath, new ByteArrayIoSupplier(out.toByteArray()));
							}
						}
					}
				}
			}
		}
	}
}
