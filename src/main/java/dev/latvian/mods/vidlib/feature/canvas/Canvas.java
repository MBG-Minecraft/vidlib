package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.latvian.mods.klib.gl.GLDebugLog;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;

public class Canvas implements Consumer<RenderPass> {
	public static final Lazy<Map<ResourceLocation, Canvas>> ALL = Lazy.map(map -> {
		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof Canvas canvas) {
				map.put(canvas.id, canvas);
			}
		}
	});

	public static Canvas createExternal(ResourceLocation id) {
		return new ExternalCanvas(id);
	}

	public static Canvas createInternal(ResourceLocation id) {
		return new InternalCanvas(id);
	}

	@AutoRegister(Dist.CLIENT)
	public static final Canvas MAIN_BEFORE_PARTICLES = createExternal(VidLib.id("main_before_particles"));

	@AutoRegister(Dist.CLIENT)
	public static final Canvas MAIN_AFTER_PARTICLES = createExternal(VidLib.id("main_after_particles"));

	// @AutoRegister(Dist.CLIENT)
	// public static final Canvas CHROMATIC_ABERRATION = createExternal(VidLib.id("chromatic_aberration"));

	@AutoRegister(Dist.CLIENT)
	public static final Canvas WEAK_OUTLINE = createExternal(VidLib.id("weak_outline"));

	@AutoRegister(Dist.CLIENT)
	public static final Canvas STRONG_OUTLINE = createExternal(VidLib.id("strong_outline"));

	public final ResourceLocation id;
	public final String idString;
	public final ResourceLocation colorTexturePath;
	public final ResourceLocation depthTexturePath;
	public final String pathString;
	public final Set<ResourceLocation> defaultTargets;
	public final List<CanvasPassModifier> passModifiers;

	public boolean enabled;
	public boolean active;
	public boolean previewColor;
	public boolean previewDepth;
	public CanvasData data;
	public Consumer<Minecraft> tickCallback;
	public Consumer<Minecraft> drawSetupCallback;
	public Consumer<Minecraft> drawCallback;

	ResourceHandle<RenderTarget> outputTarget;
	private RenderPipeline renderPipeline;
	private RenderStateShard.OutputStateShard outputStateShard;

	protected Canvas(ResourceLocation id) {
		this.id = id;
		this.idString = id.toString();
		this.colorTexturePath = id.withPath(p -> "textures/vidlib/generated/canvas/color/" + p + ".png");
		this.depthTexturePath = id.withPath(p -> "textures/vidlib/generated/canvas/depth/" + p + ".png");
		this.pathString = "vidlib_framebuffer/" + id;
		this.defaultTargets = Set.of(id);
		this.passModifiers = new ArrayList<>(0);

		this.enabled = false;
		this.active = false;
		this.previewColor = false;
		this.previewDepth = false;
		this.data = CanvasData.DEFAULT;
	}

	public void markActive() {
		if (!active) {
			GLDebugLog.message("[VidLib] Activated canvas " + idString);
		}

		active = true;
	}

	public Canvas setTickCallback(Consumer<Minecraft> callback) {
		this.tickCallback = callback;
		return this;
	}

	public Canvas setDrawSetupCallback(Consumer<Minecraft> callback) {
		this.drawSetupCallback = callback;
		return this;
	}

	public Canvas setDrawCallback(Consumer<Minecraft> callback) {
		this.drawCallback = callback;
		return this;
	}

	public <T extends CanvasPassModifier> T modifier(T modifier) {
		passModifiers.add(modifier);
		return modifier;
	}

	public CanvasSampler sampler(String name) {
		return modifier(new CanvasSampler(name));
	}

	public CanvasIntUniform intUniform(String name) {
		return modifier(new CanvasIntUniform(name, UniformType.INT));
	}

	public CanvasIntUniform ivec3Uniform(String name) {
		return modifier(new CanvasIntUniform(name, UniformType.IVEC3));
	}

	public CanvasFloatUniform floatUniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.FLOAT));
	}

	public CanvasFloatUniform vec2Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.VEC2));
	}

	public CanvasFloatUniform vec3Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.VEC3));
	}

	public CanvasFloatUniform vec4Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.VEC4));
	}

	public CanvasFloatUniform mat4Uniform(String name) {
		return modifier(new CanvasFloatUniform(name, UniformType.MATRIX4X4));
	}

	public boolean draw(Minecraft mc, GpuTexture texture) {
		if (!active) {
			return false;
		}

		if (drawCallback != null) {
			drawCallback.accept(mc);
		}

		var t = getColorTexture();

		if (t != null) {
			var sequentialBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
			var ibuf = sequentialBuffer.getBuffer(6);
			var vbuf = RenderSystem.getQuadVertexBuffer();

			try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(texture, data.clearColor().isTransparent() ? OptionalInt.empty() : OptionalInt.of(data.clearColor().argb()))) {
				pass.setPipeline(getRenderPipeline());
				pass.setVertexBuffer(0, vbuf);
				pass.setIndexBuffer(ibuf, sequentialBuffer.type());
				pass.bindSampler("InSampler", t);
				pass.drawIndexed(0, 6);
				return true;
			}
		}

		return false;
	}

	public void clear() {
		var c = getColorTexture();
		var d = getDepthTexture();

		if (c == null) {
			throw new IllegalStateException("Color texture missing from Canvas " + idString);
		} else if (d == null) {
			throw new IllegalStateException("Depth texture missing from Canvas " + idString);
		}

		RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(c, data.clearColor().argb(), d, 1D);
	}

	public void copyColorFrom(@Nullable RenderTarget from) {
		var c = getColorTexture();

		if (c == null) {
			throw new IllegalStateException("Trying to copy color texture to a RenderTarget without a color texture");
		} else if (from == null || from.getColorTexture() == null) {
			throw new IllegalStateException("Trying to copy color texture from a RenderTarget without a color texture");
		} else {
			RenderSystem.getDevice()
				.createCommandEncoder()
				.copyTextureToTexture(from.getColorTexture(), c, 0, 0, 0, 0, 0, c.getWidth(0), c.getHeight(0));
		}
	}

	public void copyDepthFrom(@Nullable RenderTarget from) {
		var d = getDepthTexture();

		if (d == null) {
			throw new IllegalStateException("Trying to copy depth texture to a RenderTarget without a depth texture");
		} else if (from == null || from.getDepthTexture() == null) {
			throw new IllegalStateException("Trying to copy depth texture from a RenderTarget without a depth texture");
		} else {
			RenderSystem.getDevice()
				.createCommandEncoder()
				.copyTextureToTexture(from.getDepthTexture(), d, 0, 0, 0, 0, 0, d.getWidth(0), d.getHeight(0));
		}
	}

	public void clone(@Nullable RenderTarget from, boolean color, boolean depth) {
		var c = getColorTexture();
		var d = getDepthTexture();

		if (c == null) {
			throw new IllegalStateException("Color texture missing from Canvas " + idString);
		} else if (d == null) {
			throw new IllegalStateException("Depth texture missing from Canvas " + idString);
		}

		var device = RenderSystem.getDevice();
		device.createCommandEncoder().clearColorAndDepthTextures(c, data.clearColor().argb(), d, 1D);

		if (color) {
			if (from == null || from.getColorTexture() == null) {
				throw new IllegalStateException("Trying to copy color texture from a RenderTarget without a color texture");
			} else {
				RenderSystem.getDevice()
					.createCommandEncoder()
					.copyTextureToTexture(from.getColorTexture(), c, 0, 0, 0, 0, 0, c.getWidth(0), c.getHeight(0));
			}
		}

		if (depth) {
			if (from == null || from.getDepthTexture() == null) {
				throw new IllegalStateException("Trying to copy depth texture from a RenderTarget without a depth texture");
			} else {
				RenderSystem.getDevice()
					.createCommandEncoder()
					.copyTextureToTexture(from.getDepthTexture(), d, 0, 0, 0, 0, 0, d.getWidth(0), d.getHeight(0));
			}
		}
	}

	@Nullable
	@ApiStatus.Internal
	public ResourceHandle<RenderTarget> getOutputTargetResource() {
		return outputTarget;
	}

	@Nullable
	public RenderTarget getOutputTarget() {
		return null;
	}

	@Nullable
	public GpuTexture getColorTexture() {
		var t = getOutputTarget();
		return t == null ? null : t.getColorTexture();
	}

	@Nullable
	public GpuTexture getDepthTexture() {
		var t = getOutputTarget();
		return t == null ? null : t.getDepthTexture();
	}

	public int getARGB(int x, int y) {
		var c = getColorTexture();

		if (c == null) {
			return 0;
		}

		int w = c.getWidth(0);
		int h = c.getHeight(0);

		if (x < 0 || x >= w) {
			x = w / 2;
		}

		if (y < 0 || y >= h) {
			y = h / 2;
		}

		int prev = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		var device = (GlDevice) RenderSystem.getDevice();
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, ((GlTexture) c).getFbo(device.directStateAccess(), null));
		int[] pixels = new int[1];
		GL11.glReadPixels(x, y, 1, 1, GlConst.toGlExternalId(c.getFormat()), GlConst.toGlType(c.getFormat()), pixels);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prev);
		return ARGB.fromABGR(pixels[0]);
	}

	public int getCenterARGB() {
		return getARGB(-1, -1);
	}

	public float getDepth(int x, int y) {
		var c = getColorTexture();
		var d = getDepthTexture();

		if (c == null || d == null) {
			return 1F;
		}

		int w = c.getWidth(0);
		int h = c.getHeight(0);

		if (x < 0 || x >= w) {
			x = w / 2;
		}

		if (y < 0 || y >= h) {
			y = h / 2;
		}

		int prev = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		var device = (GlDevice) RenderSystem.getDevice();
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, ((GlTexture) c).getFbo(device.directStateAccess(), d));
		float[] pixels = new float[1];
		GL11.glReadPixels(x, y, 1, 1, GlConst.toGlExternalId(d.getFormat()), GlConst.toGlType(d.getFormat()), pixels);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prev);
		return pixels[0];
	}

	public float getCenterDepth() {
		return getDepth(-1, -1);
	}

	@Nullable
	public RenderTarget getTargetOrNull() {
		return outputTarget != null ? outputTarget.get() : null;
	}

	public RenderTarget getTargetOrMain() {
		var t = getTargetOrNull();
		return t != null ? t : Minecraft.getInstance().getMainRenderTarget();
	}

	public RenderPipeline getRenderPipeline() {
		if (renderPipeline == null) {
			renderPipeline = VidLibRenderPipelines.CANVAS_PIPELINES.apply(data.blendFunction());
		}

		return renderPipeline;
	}

	public RenderStateShard.OutputStateShard getOutputStateShard() {
		if (outputStateShard == null) {
			outputStateShard = new RenderStateShard.OutputStateShard(pathString, this::getTargetOrMain);
		}

		return outputStateShard;
	}

	@Override
	public String toString() {
		return idString;
	}

	public void readsAndWrites(FramePass pass) {
		if (outputTarget != null && active) {
			outputTarget = pass.readsAndWrites(outputTarget);
		}
	}

	public void createHandle(FrameGraphBuilder builder, RenderTargetDescriptor targetDescriptor) {
	}

	@Override
	public void accept(RenderPass pass) {
		for (var passModifier : passModifiers) {
			passModifier.apply(pass);
		}
	}
}
