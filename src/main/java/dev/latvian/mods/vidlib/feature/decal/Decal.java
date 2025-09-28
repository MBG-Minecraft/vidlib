package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.ClientMatrices;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.canvas.CanvasFloatUniform;
import dev.latvian.mods.vidlib.feature.canvas.CanvasIntUniform;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Decal {
	@ClientAutoRegister
	public static final Canvas CANVAS = Canvas.createExternal(VidLib.id("decals")).setDrawSetupCallback(Decal::setup);

	// public static final CanvasSampler TEXTURE = CANVAS.sampler("DecalsSampler");
	public static final CanvasIntUniform COUNT = CANVAS.intUniform("DecalCount");
	public static final CanvasFloatUniform INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM = CANVAS.mat4Uniform("InverseViewProjectionMat");

	private static final List<Decal> TEMP_LIST = new ArrayList<>();

	private static void setup(Minecraft mc) {
		var decals = mc.player.vl$sessionData().decals;

		if (!decals.isEmpty()) {
			for (var decal : decals) {
				if (decal.type != DecalType.NONE && decal.end > 0F && (decal.startColor.alpha() > 0 || decal.endColor.alpha() > 0)) {
					TEMP_LIST.add(decal);
				}
			}
		}

		if (!TEMP_LIST.isEmpty()) {
			var texture = mc.getTextureManager().byPath.get(DecalTexture.ID);

			if (texture == null) {
				texture = new DecalTexture();
				mc.getTextureManager().register(DecalTexture.ID, texture);
			}

			((DecalTexture) texture).update(TEMP_LIST, mc.gameRenderer.getMainCamera().getPosition());
			// TEXTURE.set(texture.getTexture());
			COUNT.set(TEMP_LIST.size());
			INVERSE_VIEW_PROJECTION_MATRIX_UNIFORM.set(ClientMatrices.INVERSE_WORLD);
			CANVAS.markActive();
			TEMP_LIST.clear();
		}
	}

	public DecalType type = DecalType.SPHERE;
	public Vector3d position = new Vector3d();
	public float start = 0F;
	public float end = 1F;
	public float grid = 0F;
	public float thickness = 0.0625F;
	public float height = 1F;
	public float rotation = 0F;
	public Color startColor = Color.WHITE;
	public Color endColor = Color.WHITE;
	public boolean surface = false;
	public boolean additive = false;

	public void setPosition(Position p) {
		position.set(p.x(), p.y(), p.z());
	}

	public void upload(IntArrayList arr, Vec3 cameraPos) {
		arr.add((type.shaderId & 7)
			| (surface ? 8 : 0)
			| (additive ? 16 : 0)
		);

		arr.add(Float.floatToIntBits((float) (position.x - cameraPos.x))); // 1
		arr.add(Float.floatToIntBits((float) (position.y - cameraPos.y))); // 2
		arr.add(Float.floatToIntBits((float) (position.z - cameraPos.z))); // 3

		arr.add(Float.floatToIntBits(start)); // 4
		arr.add(Float.floatToIntBits(end)); // 5
		arr.add(Float.floatToIntBits(height)); // 6
		arr.add(Float.floatToIntBits((float) Math.toRadians(rotation))); // 7

		arr.add(startColor.argb()); // 8
		arr.add(endColor.argb()); // 9
		arr.add(Float.floatToIntBits(grid)); // 10
		arr.add(Float.floatToIntBits(thickness)); // 11
	}
}
