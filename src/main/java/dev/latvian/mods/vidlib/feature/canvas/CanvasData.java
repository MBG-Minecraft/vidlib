package dev.latvian.mods.vidlib.feature.canvas;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibClientCodecs;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.ID;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record CanvasData(
	int priority,
	boolean autoDraw,
	boolean autoClear,
	boolean alwaysActive,
	Set<ResourceLocation> importTargets,
	boolean depth,
	boolean stencil,
	float scale,
	Color clearColor,
	BlendFunction blendFunction,
	boolean gui
) {
	public static final CanvasData DEFAULT = new CanvasData(
		0,
		true,
		true,
		true,
		java.util.Set.of(),
		true,
		false,
		1F,
		Color.TRANSPARENT,
		BlendFunction.ENTITY_OUTLINE_BLIT,
		false
	);

	public static final Codec<CanvasData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.INT.optionalFieldOf("priority", 0).forGetter(CanvasData::priority),
		Codec.BOOL.optionalFieldOf("auto_draw", true).forGetter(CanvasData::autoDraw),
		Codec.BOOL.optionalFieldOf("auto_clear", true).forGetter(CanvasData::autoClear),
		Codec.BOOL.optionalFieldOf("always_active", false).forGetter(CanvasData::alwaysActive),
		KLibCodecs.setOf(ID.CODEC).optionalFieldOf("import", Set.of()).forGetter(CanvasData::importTargets),
		Codec.BOOL.optionalFieldOf("depth", true).forGetter(CanvasData::depth),
		Codec.BOOL.optionalFieldOf("stencil", false).forGetter(CanvasData::stencil),
		Codec.FLOAT.optionalFieldOf("scale", 1F).forGetter(CanvasData::scale),
		Color.CODEC.optionalFieldOf("clear_color", Color.TRANSPARENT).forGetter(CanvasData::clearColor),
		KLibClientCodecs.BLEND_FUNCTION.optionalFieldOf("blend_function", BlendFunction.ENTITY_OUTLINE_BLIT).forGetter(CanvasData::blendFunction),
		Codec.BOOL.optionalFieldOf("gui", false).forGetter(CanvasData::gui)
	).apply(instance, CanvasData::new));
}
