package dev.latvian.mods.vidlib.feature.prop.builtin.shape;

import dev.latvian.mods.klib.color.Color;
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
	public Color color;
	public Color outlineColor;
	public RenderLightLayer lightLayer;
	public Identifier texture;
	public UV uv;

	@Override
	public void extract(ShapeProp prop, VidLibExtractContext ctx) {
		var progress = prop.getRelativeTick(ctx.delta(), 1F);
		shape = prop.shape.value();
		width = (float) prop.width;
		height = (float) prop.height;

		rotate.identity();
		rotate.rotateY((float) Math.toDegrees(prop.getYaw(ctx.delta())));
		rotate.rotateX((float) Math.toDegrees(prop.getPitch(ctx.delta())));

		color = prop.color.value().get(progress);
		outlineColor = prop.canInteract && ctx.mc().hitResult instanceof PropHitResult hit && hit.prop == prop ? Color.WHITE : prop.outlineColor.value().get(progress);
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

		ctx.submitQuads(Identity.VEC_3, shape, color);
		ctx.submitLines(Identity.VEC_3, shape, outlineColor);
		ms.popPose();
	}
}
