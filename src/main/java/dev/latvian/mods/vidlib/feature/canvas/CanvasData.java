package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.vidlib.feature.codec.VLClientCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record CanvasData(
	int priority,
	boolean autoDraw,
	Set<ResourceLocation> targets,
	boolean depth,
	boolean stencil,
	float scale,
	Color clearColor,
	BlendFunction blendFunction
) {
	public static final CanvasData DEFAULT = new CanvasData(
		0,
		true,
		Set.of(),
		true,
		false,
		1F,
		Color.TRANSPARENT,
		BlendFunction.ENTITY_OUTLINE_BLIT
	);

	public static final Codec<CanvasData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("priority", 0).forGetter(CanvasData::priority),
		Codec.BOOL.optionalFieldOf("auto_draw", true).forGetter(CanvasData::autoDraw),
		VLCodecs.set(ResourceLocation.CODEC).optionalFieldOf("targets", Set.of()).forGetter(CanvasData::targets),
		Codec.BOOL.optionalFieldOf("depth", true).forGetter(CanvasData::depth),
		Codec.BOOL.optionalFieldOf("stencil", false).forGetter(CanvasData::stencil),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(CanvasData::scale),
		Color.CODEC.optionalFieldOf("clear_color", Color.TRANSPARENT).forGetter(CanvasData::clearColor),
		VLClientCodecs.BLEND_FUNCTION.optionalFieldOf("blend_function", BlendFunction.ENTITY_OUTLINE_BLIT).forGetter(CanvasData::blendFunction)
	).apply(instance, CanvasData::new));
}
