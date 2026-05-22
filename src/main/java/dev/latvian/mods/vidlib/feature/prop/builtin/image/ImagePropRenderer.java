package dev.latvian.mods.vidlib.feature.prop.builtin.image;

import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.EntityRenderTypes;
import dev.latvian.mods.vidlib.feature.prop.PropRenderContext;
import dev.latvian.mods.vidlib.feature.prop.PropRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class ImagePropRenderer implements PropRenderer<ImageProp> {
	@ClientAutoRegister
	public static final Holder HOLDER = new Holder(ImageProp.TYPE, new ImagePropRenderer());

	@Override
	public void render(PropRenderContext<ImageProp> ctx) {
		var prop = ctx.prop();
		float delta = ctx.delta();

		var cachedData = prop.cachedData;

		if (cachedData == null) {
			cachedData = new CachedImageData();
			prop.cachedData = cachedData;
		}

		float width = (float) prop.width;
		float height = (float) prop.height;
		float y = -height;

		var bbMat = new Matrix4f();
		// bbMat.scale(width, height, 1F);
		bbMat.rotateY((float) Math.toRadians(180F - prop.getYaw(delta)));
		bbMat.rotateX((float) Math.toRadians(prop.getPitch(delta)));

		if (prop.centered) {
			bbMat.translate(0F, height / 2F, 0F);
		}

		var matrix4f = ctx.frame().poseStack().last().pose();
		matrix4f.mul(bbMat);

		int light = prop.fullBright ? LightTexture.FULL_BRIGHT : prop.getPackedLight();
		float w2 = width / 2F;

		int tint = prop.tint.argb();

		// var buffer = ctx.frame().buffers().getBuffer(prop.seeThrough ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
		var buffer = ctx.frame().buffers().getBuffer(EntityRenderTypes.texture(prop.texture, prop.translucent));
		buffer.addVertex(matrix4f, -w2, y, 0F).setUv(1F, 1F).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
		buffer.addVertex(matrix4f, -w2, 0F, 0F).setUv(1F, 0F).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
		buffer.addVertex(matrix4f, w2, 0F, 0F).setUv(0F, 0F).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);
		buffer.addVertex(matrix4f, w2, y, 0F).setUv(0F, 1F).setColor(tint).setLight(light).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0F, 1F, 0F);

		cachedData.va.set(-w2, y, 0F).mulPosition(bbMat);
		cachedData.vb.set(-w2, 0F, 0F).mulPosition(bbMat);
		cachedData.vc.set(w2, 0F, 0F).mulPosition(bbMat);
		cachedData.vd.set(w2, y, 0F).mulPosition(bbMat);

		cachedData.box = new AABB(
			Math.min(Math.min(cachedData.va.x, cachedData.vb.x), Math.min(cachedData.vc.x, cachedData.vd.x)) - 0.05F,
			Math.min(Math.min(cachedData.va.y, cachedData.vb.y), Math.min(cachedData.vc.y, cachedData.vd.y)) - 0.05F,
			Math.min(Math.min(cachedData.va.z, cachedData.vb.z), Math.min(cachedData.vc.z, cachedData.vd.z)) - 0.05F,
			Math.max(Math.max(cachedData.va.x, cachedData.vb.x), Math.max(cachedData.vc.x, cachedData.vd.x)) + 0.05F,
			Math.max(Math.max(cachedData.va.y, cachedData.vb.y), Math.max(cachedData.vc.y, cachedData.vd.y)) + 0.05F,
			Math.max(Math.max(cachedData.va.z, cachedData.vb.z), Math.max(cachedData.vc.z, cachedData.vd.z)) + 0.05F
		);
	}
}
