package dev.latvian.mods.vidlib.feature.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.texture.LightUV;
import dev.latvian.mods.klib.texture.OverlayUV;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;

public class MSDFRenderer {
	private MSDFFont font = MSDFFont.UNKNOWN;
	private int topLeftColor = 0xFFFFFFFF;
	private int topRightColor = 0xFFFFFFFF;
	private int bottomLeftColor = 0xFFFFFFFF;
	private int bottomRightColor = 0xFFFFFFFF;
	private float shadow = 0F;
	private float shadowOffset = 0.075F;
	private float shadowDepth = 0F;
	private boolean seeThrough = false;
	private float normalX = 0F;
	private float normalY = 1F;
	private float normalZ = 0F;
	private LightUV packedLight = LightUV.FULLBRIGHT;
	private OverlayUV packedOverlay = OverlayUV.NORMAL;
	private float slant = 0F;

	private float cursorX = 0F;
	private float cursorY = 0F;
	private float lineHeight = 0F;

	private final Vector3f normal = new Vector3f();

	public MSDFRenderer setFont(MSDFFont font) {
		this.font = font;
		return this;
	}

	public MSDFRenderer setFont(ResourceKey<MSDFFont> font) {
		return setFont(MSDFFont.getFont(font));
	}

	public MSDFFont getFont() {
		return font;
	}

	public MSDFRenderer setCornerColors(int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
		this.topLeftColor = topLeftColor;
		this.topRightColor = topRightColor;
		this.bottomLeftColor = bottomLeftColor;
		this.bottomRightColor = bottomRightColor;
		return this;
	}

	public MSDFRenderer setCornerColors(Color topLeftColor, Color topRightColor, Color bottomLeftColor, Color bottomRightColor) {
		return setCornerColors(topLeftColor.argb(), topRightColor.argb(), bottomLeftColor.argb(), bottomRightColor.argb());
	}

	public MSDFRenderer setColor(int color) {
		return setCornerColors(color, color, color, color);
	}

	public MSDFRenderer setColor(Color color) {
		return setCornerColors(color, color, color, color);
	}

	public MSDFRenderer setShadow(float shadow) {
		this.shadow = shadow;
		return this;
	}

	public MSDFRenderer setShadow(boolean shadow) {
		return setShadow(shadow ? 0.25F : 0F);
	}

	public MSDFRenderer setShadowOffset(float shadowOffset) {
		this.shadowOffset = shadowOffset;
		return this;
	}

	public MSDFRenderer setShadowDepth(float shadowDepth) {
		this.shadowDepth = shadowDepth;
		return this;
	}

	public MSDFRenderer setSeeThrough(boolean seeThrough) {
		this.seeThrough = seeThrough;
		return this;
	}

	public MSDFRenderer setNormal(float x, float y, float z) {
		this.normalX = x;
		this.normalY = y;
		this.normalZ = z;
		return this;
	}

	public MSDFRenderer setPackedLight(LightUV packedLight) {
		this.packedLight = packedLight;
		return this;
	}

	public MSDFRenderer setPackedOverlay(OverlayUV packedOverlay) {
		this.packedOverlay = packedOverlay;
		return this;
	}

	public MSDFRenderer setSlant(float slant) {
		this.slant = slant;
		return this;
	}

	public MSDFRenderer setItalic(boolean italic) {
		return setSlant(italic ? font.data().italic() : 0F);
	}

	//

	public void resetCursor() {
		setCursor(0F, 0F);
	}

	public void setCursor(float x, float y) {
		cursorX = x;
		cursorY = y;
	}

	public float getCursorX() {
		return cursorX;
	}

	public float getCursorY() {
		return cursorY;
	}

	public float getLineHeight() {
		return lineHeight;
	}

	//

	public void draw(GuiGraphics graphics, String text) {
		draw(graphics.pose(), graphics.vl$buffers(), text);
	}

	public void draw(PoseStack poseStack, MultiBufferSource buffers, String text) {
		RenderSystem.setModelOffset(0F, 0F, 0F);
		var buffer = buffers.getBuffer(seeThrough ? font.seeThroughRenderType() : font.renderType());
		addVertices(poseStack, buffer, text, 0xFFFFFFFF);
	}

	public void addVertices(PoseStack ms, VertexConsumer buffer, String text, int tint) {
		var pose = ms.last();

		if (shadow > 0F) {
			addVertices(pose, buffer, text, tint, true);
		}

		addVertices(pose, buffer, text, tint, false);
	}

	public void newLine() {
		if (cursorX == 0F && lineHeight == 0F) {
			lineHeight = font.data().metrics().lineHeight();
		}

		cursorY += lineHeight;
		cursorX = 0F;
		lineHeight = 0F;
	}

	public void addVertices(PoseStack.Pose pose, VertexConsumer buffer, String text, int tint, boolean isShadow) {
		var data = font.data();
		float z = isShadow ? shadowDepth : 0F;
		int len = text.length();

		int cTopRight0 = ARGB.multiply(topRightColor, tint);
		int cTopLeft0 = ARGB.multiply(topLeftColor, tint);
		int cBottomLeft0 = ARGB.multiply(bottomLeftColor, tint);
		int cBottomRight0 = ARGB.multiply(bottomRightColor, tint);
		int cTopRight = isShadow ? ARGB.scaleRGB(cTopRight0, shadow) : cTopRight0;
		int cTopLeft = isShadow ? ARGB.scaleRGB(cTopLeft0, shadow) : cTopLeft0;
		int cBottomLeft = isShadow ? ARGB.scaleRGB(cBottomLeft0, shadow) : cBottomLeft0;
		int cBottomRight = isShadow ? ARGB.scaleRGB(cBottomRight0, shadow) : cBottomRight0;

		var pos = new Vector3f();
		pose.transformNormal(normalX, normalY, normalZ, normal);
		var m = pose.pose();

		int lightU = packedLight.u();
		int lightV = packedLight.v();
		int overlayU = packedOverlay.u();
		int overlayV = packedOverlay.v();

		char prevChar = 0;

		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);

			if (c == '\n') {
				prevChar = '\n';
				newLine();
				continue;
			}

			var g = font.getGlyph(c);

			if (g != null) {
				var uv = g.uv();
				var p = g.plane();
				var kerning = g.kerning().get(prevChar);

				if (p != null && uv != null) {
					float x = cursorX + (isShadow ? shadowOffset : 0F);
					float y = cursorY + (isShadow ? shadowOffset : 0F) + data.metrics().lineHeight() + data.metrics().descender();

					pos.set(x + p.left(), y - p.bottom(), z);
					pos.mulPosition(m);
					buffer.addVertex(pos.x, pos.y, pos.z).setUv(uv.left(), uv.top()).setColor(cBottomLeft).setUv1(overlayU, overlayV).setUv2(lightU, lightV).setNormal(normal.x, normal.y, normal.z);

					pos.set(x + p.right(), y - p.bottom(), z);
					pos.mulPosition(m);
					buffer.addVertex(pos.x, pos.y, pos.z).setUv(uv.right(), uv.top()).setColor(cBottomRight).setUv1(overlayU, overlayV).setUv2(lightU, lightV).setNormal(normal.x, normal.y, normal.z);

					pos.set(x + p.right() + slant, y - p.top(), z);
					pos.mulPosition(m);
					buffer.addVertex(pos.x, pos.y, pos.z).setUv(uv.right(), uv.bottom()).setColor(cTopRight).setUv1(overlayU, overlayV).setUv2(lightU, lightV).setNormal(normal.x, normal.y, normal.z);

					pos.set(x + p.left() + slant, y - p.top(), z);
					pos.mulPosition(m);
					buffer.addVertex(pos.x, pos.y, pos.z).setUv(uv.left(), uv.bottom()).setColor(cTopLeft).setUv1(overlayU, overlayV).setUv2(lightU, lightV).setNormal(normal.x, normal.y, normal.z);
				}

				cursorX += g.advance() + kerning;
				lineHeight = Math.max(lineHeight, data.metrics().lineHeight());
				prevChar = c;
			}
		}
	}
}
