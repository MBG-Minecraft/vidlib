package dev.latvian.mods.vidlib.feature.prop.builtin.danger;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.decal.DecalRenderer;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.util.Mth;
import org.joml.Vector3d;

public class DangerPropRenderer implements PropRenderer<DangerProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(DangerProp.TYPE, new DangerPropRenderer());

	@Override
	public void render(PropRenderContext<DangerProp> ctx) {
		var prop = ctx.prop();

		if (Double.isNaN(prop.groundY)) {
			return;
		}

		float delta = ctx.delta();
		var pos = prop.getPos(delta);
		var distance = Math.clamp(KMath.map(Math.abs(pos.y - prop.groundY), 0.5D, prop.maxHeight, 1D, 0D), 0D, 1D);
		float progress = (float) (1D - Easing.QUINT_OUT.ease(1D - distance));

		if (progress > 0F) {
			var decal = Decal.createDanger((float) (prop.width / 2D * Mth.lerp(progress, prop.widthMod, 1D)));
			decal.setPosition(new Vector3d(pos.x, prop.groundY - 0.0625D, pos.z), true);
			// decal.grid = Mth.lerp(progress, 4F, 0.25F); // optional

			if (prop.blink) {
				int blink = distance > 0.95D ? 2 : distance > 0.85D ? 4 : 0;

				if (blink > 0 && prop.tick % (blink * 2) >= blink) {
					decal.endColor = Color.WHITE.withAlpha(100);
				}
			}

			decal.applyProgress(progress);
			DecalRenderer.add(decal);
		}
	}
}
