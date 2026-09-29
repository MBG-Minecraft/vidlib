package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.DynamicTextureHolder;
import dev.latvian.mods.vidlib.util.client.DataTexture;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DecalTexture extends DataTexture {
	@ClientAutoRegister
	public static final DynamicTextureHolder<DecalTexture> HOLDER = new DynamicTextureHolder<>(ID.vidlib("effect/decals"), Lazy.of(DecalTexture::new));

	public DecalTexture() {
		super("Decal Texture", 16, 16);
	}

	public int update(List<Decal> decals, Vec3 cameraPos) {
		beginUpdate();

		for (var decal : decals) {
			var row = nextRow();
			decal.upload(row, cameraPos);
		}

		return endUpdate();
	}
}
