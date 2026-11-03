package dev.latvian.mods.vidlib.feature.prop.builtin.danger;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import org.joml.Vector3d;

public class DangerPropRenderState extends PropRenderState<DangerProp> {
	public Vector3d position = new Vector3d();

	@Override
	public void extract(DangerProp prop, VidLibExtractContext ctx) {
		invisible = true;

		if (Double.isNaN(prop.groundY)) {
			return;
		}

		var distance = Math.clamp(KMath.map(Math.abs(y - prop.groundY), 0.5D, prop.maxHeight, 1D, 0D), 0D, 1D);
		var progress = 1F - Ease.outQuint((float) (1D - distance));

		if (progress <= 0F) {
			return;
		}

		position.x = x;
		position.y = prop.groundY - 0.0625D;
		position.z = z;

		var width = (float) (prop.width / 2D * Mth.lerp(progress, prop.widthMod, 1D));

		if (width <= 0F) {
			return;
		}

		var decal = Decal.createDanger(position, width);
		// decal.grid = Mth.lerp(progress, 4F, 0.25F); // optional

		if (prop.blink) {
			int blink = distance > 0.95D ? 2 : distance > 0.85D ? 4 : 0;

			if (blink > 0 && prop.tick % (blink * 2) >= blink) {
				decal.outerColor = Color.WHITE.withAlpha(100);
			}
		}

		decal.applyProgress(progress);
		ctx.vl().addVisual(decal);
	}
}
