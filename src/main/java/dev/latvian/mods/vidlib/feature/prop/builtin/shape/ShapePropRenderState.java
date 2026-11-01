package dev.latvian.mods.vidlib.feature.prop.builtin.shape;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.render.DebugRenderTypes;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.client.ExtractContext;
import dev.latvian.mods.vidlib.client.SubmitContext;
import dev.latvian.mods.vidlib.feature.client.RenderLightLayer;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class ShapePropRenderState extends PropRenderState<ShapeProp> {
	public final Matrix4f transform = new Matrix4f();
	public Shape shape;
	public Color color;
	public Color outlineColor;
	public RenderLightLayer lightLayer;
	public Identifier texture;
	public UV uv;

	@Override
	public void extract(ShapeProp prop, ExtractContext ctx) {
		transform.identity();

		var progress = prop.getRelativeTick(ctx.delta(), 1F);
		float sw = (float) prop.width;
		float sh = (float) prop.height;

		transform.identity();
		transform.rotateY((float) Math.toDegrees(prop.getYaw(ctx.delta())));
		transform.rotateX((float) Math.toDegrees(prop.getPitch(ctx.delta())));
		transform.translate(0F, sh / 2F, 0F);

		if (sw != 1F || sh != 1F) {
			transform.scale(sw, sh, sw);
		}

		color = prop.color.value().get(progress);
		outlineColor = prop.canInteract && ctx.mc().hitResult instanceof PropHitResult hit && hit.prop == prop ? Color.WHITE : prop.outlineColor.value().get(progress);
		lightLayer = prop.lightLayer;
		texture = prop.texture == SpriteKey.EMPTY ? null : prop.texture.getTexture();
		uv = texture == null ? UV.FULL : ctx.mc().getSpriteUV(prop.texture);
	}

	@Override
	public void submit(SubmitContext ctx) {
		var ms = ctx.poseStack();
		float lineWidth = ctx.lineWidth();
		ms.pushPose();
		ms.mulPose(transform);

		if (!color.isTransparent()) {
			ctx.collector().submitCustomGeometry(ms, DebugRenderTypes.QUADS_NO_CULL_NO_DEPTH, (pose, buffer) -> shape.buildQuads(pose.transform(buffer).withColor(color)));
		}

		if (!outlineColor.isTransparent()) {
			ctx.collector().submitCustomGeometry(ms, DebugRenderTypes.LINES, (pose, buffer) -> shape.buildLines(pose.transform(buffer).withColor(outlineColor).withLineWidth(lineWidth)));
		}

		ms.popPose();
	}
}
