package dev.latvian.mods.vidlib.feature.gradient;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.color.PixelGradient;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.registry.ID;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ClientGradients extends SimplePreparableReloadListener<Map<ResourceLocation, Gradient>> {
	public static final VLRegistry<Gradient> REGISTRY = VLRegistry.createClient("gradient");

	public static final Codec<Gradient> CODEC = Codec.either(REGISTRY.valueCodec(), Color.CODEC).xmap(either -> either.map(Function.identity(), Function.identity()), gradient -> gradient instanceof Color color ? Either.right(color) : Either.left(gradient));
	public static final StreamCodec<ByteBuf, Gradient> STREAM_CODEC = ByteBufCodecs.either(REGISTRY.valueStreamCodec(), Color.STREAM_CODEC).map(either -> either.map(Function.identity(), Function.identity()), gradient -> gradient instanceof Color color ? Either.right(color) : Either.left(gradient));

	public static final RegistryRef<Gradient> FIRE_1 = REGISTRY.ref(ID.mc("fire/1"));
	public static final RegistryRef<Gradient> FIRE_2 = REGISTRY.ref(ID.mc("fire/2"));
	public static final RegistryRef<Gradient> FIRE_3 = REGISTRY.ref(ID.mc("fire/3"));
	public static final RegistryRef<Gradient> FIRE_4 = REGISTRY.ref(ID.mc("fire/4"));
	public static final RegistryRef<Gradient> SPARK = REGISTRY.ref(ID.mc("spark"));

	@Override
	protected Map<ResourceLocation, Gradient> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		var map = new HashMap<ResourceLocation, Gradient>();

		for (var entry : resourceManager.listResources("textures/vidlib/gradient", id -> !id.getPath().startsWith("_") && id.getPath().endsWith(".png")).entrySet()) {
			try (var in = entry.getValue().open()) {
				var id = entry.getKey().withPath(s -> s.substring(25, s.length() - 4));

				try (var image = NativeImage.read(in)) {
					var pixels = new Color[image.getWidth()];

					for (int i = 0; i < pixels.length; i++) {
						pixels[i] = Color.of(image.getPixel(i, 0));
					}

					map.put(id, new PixelGradient(pixels));
				}
			} catch (Exception ex) {
				VidLib.LOGGER.error("Error while reading file " + entry.getKey(), ex);
			}
		}

		return map;
	}

	@Override
	protected void apply(Map<ResourceLocation, Gradient> from, ResourceManager resourceManager, ProfilerFiller profiler) {
		REGISTRY.update(Map.copyOf(from));
		GradientCommand.GRADIENT_IDS.clear();
		GradientCommand.GRADIENT_IDS.addAll(from.keySet());
	}
}
