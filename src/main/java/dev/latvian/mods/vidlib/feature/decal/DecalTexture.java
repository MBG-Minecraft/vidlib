package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.klib.util.client.DataTexture;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.DynamicTextureHolder;
import dev.latvian.mods.vidlib.util.client.ReloadableDynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;

import java.io.BufferedReader;
import java.util.List;

public class DecalTexture extends DataTexture implements ReloadableDynamicTexture {
	public static int maxDecals = 0;

	@ClientAutoRegister
	public static final DynamicTextureHolder<DecalTexture> HOLDER = new DynamicTextureHolder<>(ID.vidlib("effect/decals"), Lazy.of(DecalTexture::new));

	public DecalTexture() {
		super("Decal Texture", 14, 1);
		update(List.of(), Vec3.ZERO);
	}

	@Override
	public void load(ResourceManager resourceManager, TextureManager manager) {
		maxDecals = 0;

		try (var reader = new BufferedReader(resourceManager.getResourceOrThrow(ID.vidlib("shaders/include/decal_config.glsl")).openAsReader())) {
			for (var line : reader.lines().toList()) {
				line = line.trim().replaceAll("\\s+", " ");

				if (line.startsWith("#define MAX_DECALS ")) {
					maxDecals = Integer.parseInt(line.substring(19));
				}
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load decal_config.glsl", ex);
		}
	}

	public int update(List<Decal> decals, Vec3 cameraPos) {
		beginUpdate();

		int maxCount = Math.min(decals.size(), maxDecals);

		for (int i = 0; i < maxCount; i++) {
			var row = nextRow();
			decals.get(i).upload(row, cameraPos);
		}

		if (maxCount < maxDecals) {
			nextRow().add(0);
		}

		return endUpdate();
	}
}
