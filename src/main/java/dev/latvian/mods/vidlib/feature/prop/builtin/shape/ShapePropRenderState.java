package dev.latvian.mods.vidlib.feature.prop.builtin.shape;

import dev.latvian.mods.klib.math.Identity;
import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;

public class ShapePropRenderState extends PropRenderState<ShapeProp> {
	public Shape shape;
	public float width;
	public float height;
	public final Quaternionf rotate = new Quaternionf();
	public int color;
	public int outlineColor;
	public RenderLightLayer lightLayer;
	public Identifier texture;
	public UV uv;

	@Override
	public void extract(ShapeProp prop, VidLibExtractContext ctx) {
		if (prop.shape == null || prop.color == null || prop.outlineColor == null) {
			invisible = true;
			return;
		}

		var progress = prop.getRelativeTick(ctx.delta(), 1F);
		shape = prop.shape.value();
		width = (float) prop.width;
		height = (float) prop.height;

		rotate.identity();
		rotate.rotateY((float) Math.toRadians(prop.getYaw(ctx.delta())));
		rotate.rotateX((float) Math.toRadians(prop.getPitch(ctx.delta())));

		color = prop.color.value().get(progress).argb();
		outlineColor = prop.canInteract && ctx.mc().hitResult instanceof PropHitResult hit && hit.prop == prop ? 0xFFFFFFFF : prop.outlineColor.value().get(progress).argb();
		lightLayer = prop.lightLayer;
		texture = prop.texture == SpriteKey.EMPTY ? null : prop.texture.getTexture().texturePath();
		uv = texture == null ? UV.FULL : ctx.mc().getSpriteUV(prop.texture);
	}

	@Override
	public void submit(SubmitContext ctx) {
		var ms = ctx.poseStack();
		ms.pushPose();
		ctx.rotate(rotate);
		ctx.poseStack().translate(0F, height / 2F, 0F);

		if (width != 1F || height != 1F) {
			ctx.poseStack().scale(width, height, width);
		}

		ctx.submitQuads(shape, Identity.VEC_3, color);
		ctx.submitLines(shape, Identity.VEC_3, outlineColor);
		ms.popPose();
	}
}
