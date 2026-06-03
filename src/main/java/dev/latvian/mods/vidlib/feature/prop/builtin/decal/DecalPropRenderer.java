package dev.latvian.mods.vidlib.feature.prop.builtin.decal;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.decal.DecalRenderer;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import org.joml.Vector3d;

public class DecalPropRenderer implements PropRenderer<DecalProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = PropRenderer.holder(DecalProp.TYPE, new DecalPropRenderer());

	@Override
	public void render(PropRenderContext<DecalProp> ctx) {
		var prop = ctx.prop();
		var decal = new Decal(prop.decal);
		decal.parent = null;
		var pos = prop.getPos(ctx.delta());
		decal.position = new Vector3d(pos.x, pos.y, pos.z);
		DecalRenderer.add(decal);
	}
}
