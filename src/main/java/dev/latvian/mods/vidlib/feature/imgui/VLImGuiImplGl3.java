package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.latvian.mods.klib.util.ID;
import imgui.ImDrawData;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiBackendFlags;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class VLImGuiImplGl3 {
	private static final int FONT_TEXTURE_ID = 1;

	private static final VertexFormat VERTEX_FORMAT;

	static {
		VertexFormatElement posElement = null;

		for (int i = 7; i < VertexFormatElement.MAX_COUNT; i++) {
			if (VertexFormatElement.byId(i) == null) {
				posElement = VertexFormatElement.register(i, 0, VertexFormatElement.Type.FLOAT, false, 2);
				break;
			}
		}

		if (posElement == null) {
			throw new IllegalStateException("Failed to create ImGui vertex format");
		}

		VERTEX_FORMAT = VertexFormat.builder()
			.add("Position", posElement)
			.add("UV", VertexFormatElement.UV0)
			.add("Color", VertexFormatElement.COLOR)
			.build();
	}

	private static final RenderPipeline PIPELINE = RenderPipeline.builder()
		.withLocation(ID.vidlib("pipeline/imgui"))
		.withVertexShader(ID.vidlib("core/imgui"))
		.withFragmentShader(ID.vidlib("core/imgui"))
		.withSampler("Texture")
		.withUniform("Projection", UniformType.UNIFORM_BUFFER)
		.withColorTargetState(new ColorTargetState(Optional.of(BlendFunction.TRANSLUCENT), ColorTargetState.WRITE_ALL))
		.withCull(false)
		.withVertexFormat(VERTEX_FORMAT, VertexFormat.Mode.TRIANGLES)
		// .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
		.build();

	public int gFontTexture = FONT_TEXTURE_ID;
	public int glFontWidth = 0;
	public int glFontHeight = 0;

	private Data data = null;
	private TextureTarget screenTarget = null;
	private final ImVec4 clipRect = new ImVec4();

	public void init() {
		this.data = new Data();

		var io = ImGui.getIO();
		io.setBackendRendererName("imgui-java_impl_" + RenderSystem.getDevice().getBackendName());
		io.addBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset);

		this.updateFontsTexture();
	}

	public void newFrame() {
		if (this.data != null && this.data.fontTexture == null) {
			this.updateFontsTexture();
		}
	}

	public void renderDrawData(ImDrawData drawData) {
		this.renderDrawData(drawData, Minecraft.getInstance().getMainRenderTarget());
	}

	public void renderDrawData(ImDrawData drawData, RenderTarget mainRenderTarget) {
		if (this.data == null) {
			return;
		}

		if (drawData.getCmdListsCount() <= 0) {
			return;
		}

		RenderSystem.assertOnRenderThread();
		var window = Minecraft.getInstance().getWindow();
		int width = window.vl$getUnscaledFramebufferWidth();
		int height = window.vl$getUnscaledFramebufferHeight();

		if (width <= 0 || height <= 0) {
			return;
		}

		var target = this.getScreenTarget(width, height);
		this.prepareScreenTarget(mainRenderTarget, target);

		if (this.data.mainViewportData == null) {
			this.data.mainViewportData = new ViewportData();
		}

		this.data.mainViewportData.renderTarget = target;
		this.renderDrawData(drawData, this.data.mainViewportData, OptionalInt.empty());
		target.blitToScreen();
	}

	public void postDraw() {
		this.clearTextures();
	}

	public void dispose() {
		if (this.data == null) {
			return;
		}

		var io = ImGui.getIO();
		io.setBackendRendererName(null);
		io.removeBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset);
		this.destroyDeviceObjects();
		this.data = null;
	}

	public void updateFontsTexture() {
		if (this.data == null) {
			return;
		}

		this.destroyFontsTexture();

		ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
		ImInt width = new ImInt();
		ImInt height = new ImInt();
		ByteBuffer pixels = fontAtlas.getTexDataAsRGBA32(width, height);

		this.glFontWidth = width.get();
		this.glFontHeight = height.get();

		GpuDevice device = RenderSystem.getDevice();
		this.data.fontTexture = device.createTexture(
			"ImGui Font Atlas",
			GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
			TextureFormat.RGBA8,
			this.glFontWidth,
			this.glFontHeight,
			1,
			1
		);
		device.createCommandEncoder().writeToTexture(
			this.data.fontTexture,
			pixels,
			NativeImage.Format.RGBA,
			0,
			0,
			0,
			0,
			this.glFontWidth,
			this.glFontHeight
		);
		this.data.fontTextureView = device.createTextureView(this.data.fontTexture);
		this.gFontTexture = FONT_TEXTURE_ID;
		fontAtlas.setTexID(FONT_TEXTURE_ID);
	}

	public int getTextureId(@Nullable GpuTexture texture) {
		if (this.data == null || texture == null || texture.isClosed()) {
			return 0;
		}

		var id = this.data.textureIds.get(texture);

		if (id != null) {
			return id;
		}

		var view = RenderSystem.getDevice().createTextureView(texture);
		id = this.registerTexture(view, true);
		this.data.textureIds.put(texture, id);
		return id;
	}

	public int getTextureId(@Nullable GpuTextureView view) {
		if (this.data == null || view == null || view.isClosed()) {
			return 0;
		}

		var id = this.data.textureViewIds.get(view);

		if (id != null) {
			return id;
		}

		id = this.registerTexture(view, false);
		this.data.textureViewIds.put(view, id);
		return id;
	}

	private int registerTexture(GpuTextureView view, boolean owned) {
		this.data.textures.add(new TextureBinding(view, owned));
		return this.data.textures.size() + 1;
	}

	private void prepareScreenTarget(RenderTarget mainRenderTarget, TextureTarget target) {
		GpuTextureView targetView = target.getColorTextureView();
		GpuTexture targetTexture = target.getColorTexture();
		GpuTexture mainTexture = mainRenderTarget.getColorTexture();

		if (targetView == null || targetTexture == null) {
			return;
		}

		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

		try (RenderPass ignored = commandEncoder.createRenderPass(() -> "ImGui screen clear", targetView, OptionalInt.of(0))) {
		}

		if (mainTexture == null) {
			return;
		}

		var window = Minecraft.getInstance().getWindow();
		int dstX = Math.max((int) (window.vl$getXOffset() * window.vl$getUnscaledFramebufferWidth()), 0);
		int dstY = Math.max((int) (window.vl$getInverseYOffset() * window.vl$getUnscaledFramebufferHeight()), 0);
		int copyWidth = Math.min(mainRenderTarget.width, target.width - dstX);
		int copyHeight = Math.min(mainRenderTarget.height, target.height - dstY);

		if (copyWidth > 0 && copyHeight > 0) {
			commandEncoder.copyTextureToTexture(mainTexture, targetTexture, 0, dstX, dstY, 0, 0, copyWidth, copyHeight);
		}
	}

	private TextureTarget getScreenTarget(int width, int height) {
		if (this.screenTarget == null) {
			this.screenTarget = new TextureTarget("ImGui Screen Target", width, height, true);
		} else if (this.screenTarget.width != width || this.screenTarget.height != height) {
			this.screenTarget.resize(width, height);
		}

		return this.screenTarget;
	}

	private void renderDrawData(ImDrawData drawData, ViewportData data, OptionalInt clearColor) {
		var device = RenderSystem.getDevice();
		var renderTarget = data.renderTarget;

		int fbWidth = (int) (drawData.getDisplaySizeX() * drawData.getFramebufferScaleX());
		int fbHeight = (int) (drawData.getDisplaySizeY() * drawData.getFramebufferScaleY());

		if (fbWidth <= 0 || fbHeight <= 0) {
			data.clearVertexData(0);
			return;
		}

		int cmdListsCount = drawData.getCmdListsCount();

		if (cmdListsCount <= 0) {
			data.clearVertexData(0);
			return;
		}

		float left = drawData.getDisplayPosX();
		float right = drawData.getDisplayPosX() + drawData.getDisplaySizeX();
		float top = drawData.getDisplayPosY();
		float bottom = drawData.getDisplayPosY() + drawData.getDisplaySizeY();

		float clipOffX = drawData.getDisplayPosX();
		float clipOffY = drawData.getDisplayPosY();
		float clipScaleX = drawData.getFramebufferScaleX();
		float clipScaleY = drawData.getFramebufferScaleY();

		if (ImDrawData.SIZEOF_IM_DRAW_IDX != data.elementSize) {
			for (var indexBuffer : data.indexData) {
				indexBuffer.close();
			}

			data.indexData.clear();
		}

		data.elementSize = ImDrawData.SIZEOF_IM_DRAW_IDX;
		data.clearVertexData(cmdListsCount);

		var commandEncoder = device.createCommandEncoder();

		for (int n = 0; n < cmdListsCount; n++) {
			int vertexBufferSize = drawData.getCmdListVtxBufferSize(n) * ImDrawData.SIZEOF_IM_DRAW_VERT;
			GpuBuffer vertexBuffer = data.getVertexBuffer(device, n, vertexBufferSize);
			int indexBufferSize = drawData.getCmdListIdxBufferSize(n) * data.elementSize;
			GpuBuffer indexBuffer = data.getIndexBuffer(device, n, indexBufferSize);

			commandEncoder.writeToBuffer(vertexBuffer.slice(0, vertexBufferSize), drawData.getCmdListVtxBufferData(n));
			commandEncoder.writeToBuffer(indexBuffer.slice(0, indexBufferSize), drawData.getCmdListIdxBufferData(n));
		}

		if (data.projectionMatrixBuffer == null) {
			data.projectionMatrixBuffer = new CachedImguiOrthoBuffer(-1F, 1F);
		}

		var projectionMatrixBuffer = data.projectionMatrixBuffer.getBuffer(left, right, bottom, top);
		var colorTexture = renderTarget.getColorTextureView();
		// var depthTexture = renderTarget.getDepthTextureView();

		if (colorTexture == null/* || depthTexture == null*/) {
			return;
		}

		int width = colorTexture.getWidth(0);
		int height = colorTexture.getHeight(0);
		var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);

		try (var renderPass = commandEncoder.createRenderPass(() -> "ImGui", colorTexture, clearColor/*, depthTexture, OptionalDouble.empty()*/)) {
			renderPass.setPipeline(PIPELINE);
			renderPass.setUniform("Projection", projectionMatrixBuffer);

			for (int n = 0; n < cmdListsCount; n++) {
				GpuBuffer vertexBuffer = data.vertexData.get(n);
				GpuBuffer indexBuffer = data.indexData.get(n);
				renderPass.setVertexBuffer(0, vertexBuffer);
				renderPass.setIndexBuffer(indexBuffer, data.elementSize == 2 ? VertexFormat.IndexType.SHORT : VertexFormat.IndexType.INT);

				int cmdBufferSize = drawData.getCmdListCmdBufferSize(n);

				for (int cmdIdx = 0; cmdIdx < cmdBufferSize; cmdIdx++) {
					drawData.getCmdListCmdBufferClipRect(n, cmdIdx, this.clipRect);

					float clipMinX = (this.clipRect.x - clipOffX) * clipScaleX;
					float clipMinY = (this.clipRect.y - clipOffY) * clipScaleY;
					float clipMaxX = (this.clipRect.z - clipOffX) * clipScaleX;
					float clipMaxY = (this.clipRect.w - clipOffY) * clipScaleY;

					if (clipMaxX <= clipMinX || clipMaxY <= clipMinY) {
						continue;
					}

					int minX = Math.max((int) clipMinX, 0);
					int minY = Math.max((int) (fbHeight - clipMaxY), 0);

					if (width < minX || height < minY) {
						continue;
					}

					int scissorWidth = clamp((int) (clipMaxX - clipMinX), 0, width - minX);
					int scissorHeight = clamp((int) (clipMaxY - clipMinY), 0, height - minY);
					renderPass.enableScissor(minX, minY, scissorWidth, scissorHeight);

					int textureId = drawData.getCmdListCmdBufferTextureId(n, cmdIdx);
					int vtxOffset = drawData.getCmdListCmdBufferVtxOffset(n, cmdIdx);
					int idxOffset = drawData.getCmdListCmdBufferIdxOffset(n, cmdIdx);
					int elemCount = drawData.getCmdListCmdBufferElemCount(n, cmdIdx);

					renderPass.bindTexture("Texture", this.getTextureView(textureId), sampler);
					renderPass.drawIndexed(vtxOffset, idxOffset, elemCount, 1);
				}
			}
		}
	}

	private @Nullable GpuTextureView getTextureView(int textureId) {
		if (textureId <= FONT_TEXTURE_ID) {
			return this.data.fontTextureView;
		}

		int index = textureId - 2;
		return index >= 0 && index < this.data.textures.size() ? this.data.textures.get(index).view : this.data.fontTextureView;
	}

	private void clearTextures() {
		if (this.data == null) {
			return;
		}

		for (var binding : this.data.textures) {
			if (binding.owned && !binding.view.isClosed()) {
				binding.view.close();
			}
		}

		this.data.textures.clear();
		this.data.textureIds.clear();
		this.data.textureViewIds.clear();
	}

	private void destroyFontsTexture() {
		if (this.data.fontTextureView != null) {
			this.data.fontTextureView.close();
			this.data.fontTextureView = null;
		}

		if (this.data.fontTexture != null) {
			this.data.fontTexture.close();
			this.data.fontTexture = null;
			ImGui.getIO().getFonts().setTexID(0);
		}
	}

	private void destroyDeviceObjects() {
		this.clearTextures();

		if (this.data.mainViewportData != null) {
			this.data.mainViewportData.free();
			this.data.mainViewportData = null;
		}

		this.destroyFontsTexture();

		if (this.screenTarget != null) {
			this.screenTarget.destroyBuffers();
			this.screenTarget = null;
		}
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, max));
	}

	private record TextureBinding(GpuTextureView view, boolean owned) {
	}

	private static class Data {
		private GpuTextureView fontTextureView;
		private GpuTexture fontTexture;
		private ViewportData mainViewportData;
		private final List<TextureBinding> textures = new ArrayList<>();
		private final IdentityHashMap<GpuTexture, Integer> textureIds = new IdentityHashMap<>();
		private final IdentityHashMap<GpuTextureView, Integer> textureViewIds = new IdentityHashMap<>();
	}

	private static class ViewportData {
		private CachedImguiOrthoBuffer projectionMatrixBuffer;
		private final List<GpuBuffer> vertexData = new ArrayList<>();
		private final List<GpuBuffer> indexData = new ArrayList<>();
		private int elementSize;
		private RenderTarget renderTarget;

		private void clearVertexData(int maxCommands) {
			for (int i = this.vertexData.size() - 1; i >= maxCommands; i--) {
				this.vertexData.remove(i).close();
			}

			for (int i = this.indexData.size() - 1; i >= maxCommands; i--) {
				this.indexData.remove(i).close();
			}
		}

		private GpuBuffer getVertexBuffer(GpuDevice device, int index, int size) {
			return this.getBuffer(device, this.vertexData, index, size, "ImGui Vertex Buffer ", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX);
		}

		private GpuBuffer getIndexBuffer(GpuDevice device, int index, int size) {
			return this.getBuffer(device, this.indexData, index, size, "ImGui Index Buffer ", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX);
		}

		private GpuBuffer getBuffer(GpuDevice device, List<GpuBuffer> buffers, int index, int size, String label, int usage) {
			if (index >= buffers.size()) {
				GpuBuffer buffer = device.createBuffer(() -> label + index, usage, size);
				buffers.add(buffer);
				return buffer;
			}

			GpuBuffer buffer = buffers.get(index);

			if (buffer.size() >= size) {
				return buffer;
			}

			buffer.close();
			GpuBuffer newBuffer = device.createBuffer(() -> label + index, usage, size);
			buffers.set(index, newBuffer);
			return newBuffer;
		}

		private void free() {
			this.clearVertexData(0);

			if (this.projectionMatrixBuffer != null) {
				this.projectionMatrixBuffer.close();
				this.projectionMatrixBuffer = null;
			}

			this.renderTarget = null;
		}
	}

	private static class CachedImguiOrthoBuffer implements AutoCloseable {
		private final GpuBuffer buffer;
		private final GpuBufferSlice slice;
		private final float zNear;
		private final float zFar;
		private final Matrix4f projectionMatrix = new Matrix4f();

		private float left = Float.NaN;
		private float right = Float.NaN;
		private float bottom = Float.NaN;
		private float top = Float.NaN;

		private CachedImguiOrthoBuffer(float zNear, float zFar) {
			this.zNear = zNear;
			this.zFar = zFar;
			this.buffer = RenderSystem.getDevice().createBuffer(
				() -> "Projection matrix UBO ImGui",
				GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
				RenderSystem.PROJECTION_MATRIX_UBO_SIZE
			);
			this.slice = this.buffer.slice(0, RenderSystem.PROJECTION_MATRIX_UBO_SIZE);
		}

		private GpuBufferSlice getBuffer(float left, float right, float bottom, float top) {
			if (this.left != left || this.right != right || this.bottom != bottom || this.top != top) {
				Matrix4f matrix = this.projectionMatrix.setOrtho(left, right, bottom, top, this.zNear, this.zFar);

				try (MemoryStack stack = MemoryStack.stackPush()) {
					ByteBuffer buffer = Std140Builder.onStack(stack, RenderSystem.PROJECTION_MATRIX_UBO_SIZE).putMat4f(matrix).get();
					RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), buffer);
				}

				this.left = left;
				this.right = right;
				this.bottom = bottom;
				this.top = top;
			}

			return this.slice;
		}

		@Override
		public void close() {
			this.buffer.close();
		}
	}
}
