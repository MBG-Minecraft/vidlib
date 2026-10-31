package dev.latvian.mods.vidlib.feature.prop.builtin.decal;

import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.client.ExtractContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import org.joml.Vector3d;

public class DecalPropRenderState extends PropRenderState<DecalProp> {
	public Vector3d position = new Vector3d();

	@Override
	public void extract(DecalProp prop, ExtractContext ctx) {
		invisible = true;

		var decal = new Decal(prop.decal);
		decal.parent = null;
		var pos = prop.getPos(ctx.delta());
		decal.position = new Vector3d(pos.x, pos.y, pos.z);
		ctx.vl().addDecal(decal);
	}
}
