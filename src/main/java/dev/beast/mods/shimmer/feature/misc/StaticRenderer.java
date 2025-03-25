package dev.beast.mods.shimmer.feature.misc;

import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.math.UV;
import dev.beast.mods.shimmer.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class StaticRenderer {
	public static final List<StaticRenderer> ALL = new ArrayList<>();

	public static StaticRenderer create(VertexFormat.Mode mode, VertexFormat format, Consumer<BufferBuilder> build) {
		var renderer = new StaticRenderer(mode, format, build);
		ALL.add(renderer);
		return renderer;
	}

	public record CubeRenderer(BufferBuilder builder) {
		public void face(TextureAtlasSprite tex, float x0, float y0, float z0, float x1, float y1, float z1, UV uv, Direction dir, boolean cull) {
			FaceBakery.bakeQuad(new Vector3f(x0, y0, z0), new Vector3f(x1, y1, z1), new BlockElementFace(cull ? dir : null, -1, "", new BlockFaceUV(null, 0)), tex, dir, BlockModelRotation.X0_Y0, null, false, 0);

			var aint = new int[0]; // face.getVertices();
			var n = Vec3f.DIRECTIONS[dir.get3DDataValue()];
			int len = aint.length / 8;

			try (var memoryStack = MemoryStack.stackPush()) {
				var byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
				var intBuffer = byteBuffer.asIntBuffer();

				for (int i = 0; i < len; i++) {
					intBuffer.clear();
					intBuffer.put(aint, i * 8, 8);
					float x = byteBuffer.getFloat(0);
					float y = byteBuffer.getFloat(4);
					float z = byteBuffer.getFloat(8);
					float u = byteBuffer.getFloat(16);
					float v = byteBuffer.getFloat(20);
					builder.addVertex(x, y, z, 0xFFFFFFFF, u, v, OverlayTexture.NO_OVERLAY, LightTexture.FULL_BRIGHT, n.x(), n.y(), n.z());
				}
			}
		}
	}

	public static StaticRenderer createBlockModel(BiConsumer<Function<ResourceLocation, TextureAtlasSprite>, CubeRenderer> build) {
		return create(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK, builder -> build.accept(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS), new CubeRenderer(builder)));
	}

	public static final StaticRenderer MONEY = createBlockModel((textures, faces) -> {
		var texTop = textures.apply(Shimmer.id("block/money/top"));
		var texSideWE = textures.apply(Shimmer.id("block/money/side_we"));
		var texSideSN = textures.apply(Shimmer.id("block/money/side_sn"));
		faces.face(texTop, -0.5F, -0.125F, -0.25F, 0.5F, 0.125F, 0.25F, new UV(0F, 0F, 0.5F, 0.5F), Direction.DOWN, true);
		faces.face(texTop, -0.5F, -0.125F, -0.25F, 0.5F, 0.125F, 0.25F, new UV(0F, 0F, 0.5F, 0.5F), Direction.UP, true);
	});

	@AutoInit(AutoInit.Type.ASSETS_RELOADED)
	public static void reload() {
		for (var r : ALL) {
			if (r.vertexBuffer != null) {
				r.vertexBuffer.close();
				r.vertexBuffer = null;
			}
		}
	}

	public final VertexFormat.Mode mode;
	public final VertexFormat format;
	public final Consumer<BufferBuilder> build;
	private VertexBuffer vertexBuffer;

	private StaticRenderer(VertexFormat.Mode mode, VertexFormat format, Consumer<BufferBuilder> build) {
		this.mode = mode;
		this.format = format;
		this.build = build;
	}

	public VertexBuffer getBuffer() {
		if (vertexBuffer == null) {
			try (var memory = new ByteBufferBuilder(format.getVertexSize() * mode.primitiveLength * 6)) {
				var buffer = new BufferBuilder(memory, mode, format);
				build.accept(buffer);
				vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
				vertexBuffer.bind();
				vertexBuffer.upload(buffer.buildOrThrow());
				VertexBuffer.unbind();
			}
		}

		return vertexBuffer;
	}
}
