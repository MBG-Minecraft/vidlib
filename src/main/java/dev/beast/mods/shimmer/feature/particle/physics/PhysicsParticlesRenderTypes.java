package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.shader.ShaderHolder;
import net.minecraft.client.renderer.RenderType;

public class PhysicsParticlesRenderTypes extends RenderType {
	public static final VertexFormat FORMAT = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Normal", VertexFormatElement.NORMAL)
		.build();

	public static final ShaderHolder PHYSICS_SOLID_SHADER = new ShaderHolder(Shimmer.id("physics_particle/solid"), FORMAT);
	public static final ShaderHolder PHYSICS_CUTOUT_SHADER = new ShaderHolder(Shimmer.id("physics_particle/cutout"), FORMAT);
	public static final ShaderHolder PHYSICS_TRANSLUCENT_SHADER = new ShaderHolder(Shimmer.id("physics_particle/translucent"), FORMAT);

	public static final RenderType PHYSICS_SOLID = create(
		"shimmer:physics_solid",
		FORMAT,
		VertexFormat.Mode.QUADS,
		1536,
		CompositeState.builder()
			.createCompositeState(false)
	);

	public static final RenderType PHYSICS_CUTOUT = create(
		"shimmer:physics_cutout",
		FORMAT,
		VertexFormat.Mode.QUADS,
		1536,
		CompositeState.builder()
			.createCompositeState(false)
	);

	public static final RenderType PHYSICS_TRANSLUCENT = create(
		"shimmer:physics_translucent",
		FORMAT,
		VertexFormat.Mode.QUADS,
		1536,
		CompositeState.builder()
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(TRANSLUCENT_TARGET)
			.createCompositeState(false)
	);

	private PhysicsParticlesRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
		super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
	}
}
