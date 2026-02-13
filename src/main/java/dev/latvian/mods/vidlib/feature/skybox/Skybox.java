package dev.latvian.mods.vidlib.feature.skybox;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Skybox {
	public static final MenuItem MENU_ITEM = MenuItem.menu(ImIcons.SUN, "Skybox", g -> {
		if (!g.inGame) {
			return List.of();
		}

		var slist = new ArrayList<MenuItem>();

		var current = g.mc.level.getSkybox();
		var session = g.mc.player.vl$sessionData();

		for (var skybox : SkyboxData.SKYBOX_IDS) {
			var tex = session.getSkybox(skybox).loadTexture(g.mc);

			slist.add(MenuItem.item(tex.getIcon(), skybox.getPath(), skybox.equals(current), g1 -> {
				if (g1.isReplay || !g1.serverFeatures.has(Feature.SKYBOX)) {
					g1.mc.getDataMap().setSuperOverride(InternalServerData.SKYBOX, skybox);
				} else {
					g1.mc.runClientCommand("skybox set \"" + skybox + "\"");
				}
			}));
		}

		slist.add(MenuItem.SEPARATOR);

		slist.add(MenuItem.item(ImIcons.INVISIBLE, "Vanilla", Skyboxes.VANILLA.equals(current), g1 -> {
			if (g1.isReplay || !g1.serverFeatures.has(Feature.SKYBOX)) {
				g1.mc.getDataMap().setSuperOverride(InternalServerData.SKYBOX, Skyboxes.VANILLA);
			} else {
				g1.mc.runClientCommand("skybox set \"minecraft:vanilla\"");
			}
		}));

		slist.add(MenuItem.item(ImIcons.DOWNLOAD, "Export PNGs", g1 -> {
			var session1 = g1.mc.player.vl$sessionData();

			for (var skyboxId : SkyboxData.SKYBOX_IDS) {
				session1.getSkybox(skyboxId).export(g1.mc);
			}

			g1.mc.tell(Component.literal("Skyboxes exported! Click here to open the directory").setStyle(Style.EMPTY.withClickToOpen(VidLibPaths.LOCAL.get().resolve("export/skyboxes"))));
		}).remainOpen(false));

		return slist;
	}).remainOpen(true);

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

		var dir = VidLibPaths.LOCAL.get().resolve("export/skyboxes/" + data.id().getNamespace());

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
