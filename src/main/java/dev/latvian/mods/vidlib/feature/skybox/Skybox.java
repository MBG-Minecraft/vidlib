package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;

public class Skybox {
	public final SkyboxData data;
	public final ResourceLocation texture;
	public SkyboxTexture skyboxTexture;

	public Skybox(SkyboxData data) {
		this.data = data;
		this.texture = data.texture().isEmpty() ? data.id().withPath(p -> "textures/vidlib/skybox/" + p + ".png") : data.texture().get();
	}

	public SkyboxTexture loadTexture(Minecraft mc) {
		if (skyboxTexture == null) {
			var id = data.id().withPath(p -> "textures/vidlib/generated/skybox/" + p + ".png");

			if (mc.getTextureManager().byPath.get(id) instanceof SkyboxTexture tex) {
				skyboxTexture = tex;
				return skyboxTexture;
			}

			skyboxTexture = new SkyboxTexture(this, id);
			mc.getTextureManager().registerAndLoad(skyboxTexture.resourceId(), skyboxTexture);
		}

		return skyboxTexture;
	}

	public void export(Minecraft mc) {
		var path = data.id().getPath().replace('/', '_');

		var dir = FMLPaths.GAMEDIR.get().resolve("local/vidlib/export/skyboxes/" + data.id().getNamespace());

		try (var in = mc.getResourceManager().getResource(texture).orElseThrow().open()) {
			try (var src = NativeImage.read(in); var image = SkyboxTexture.process(texture, src, src.getWidth(), src.getHeight())) {
				var s = image.getHeight() / 2;

				if (Files.notExists(dir)) {
					Files.createDirectories(dir);
				}

				image.writeToFile(dir.resolve(path + ".png"));

				try (var out = new NativeImage(s * 3, s * 2, false)) {
					image.copyRect(out, s * 2, 0, 0, 0, s, s, false, false); // D
					image.copyRect(out, s, 0, s, 0, s, s, false, false); // U
					image.copyRect(out, s * 3, s, s * 2, 0, s, s, false, false); // S
					image.copyRect(out, 0, s, 0, s, s * 3, s, false, false); // WNE
					out.writeToFile(dir.resolve(path + "_3x2.png"));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
