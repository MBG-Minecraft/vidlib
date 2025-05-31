package dev.latvian.mods.vidlib.feature.highlight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.color.PairGradient;
import dev.latvian.mods.kmath.shape.CylinderShape;
import dev.latvian.mods.kmath.shape.Shape;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldposition.DynamicWorldPosition;
import dev.latvian.mods.vidlib.math.worldposition.FixedWorldPosition;
import dev.latvian.mods.vidlib.math.worldposition.WorldPosition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record TerrainHighlight(
	WorldPosition position,
	Shape shape,
	Gradient color,
	WorldPosition scale,
	int duration
) {
	public static final Codec<TerrainHighlight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WorldPosition.CODEC.fieldOf("position").forGetter(TerrainHighlight::position),
		Shape.CODEC.fieldOf("shape").forGetter(TerrainHighlight::shape),
		Gradient.CODEC.fieldOf("color").forGetter(TerrainHighlight::color),
		WorldPosition.CODEC.optionalFieldOf("scale", FixedWorldPosition.ONE.instance()).forGetter(TerrainHighlight::scale),
		Codec.INT.optionalFieldOf("duration", 20).forGetter(TerrainHighlight::duration)
	).apply(instance, TerrainHighlight::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TerrainHighlight> STREAM_CODEC = CompositeStreamCodec.of(
		WorldPosition.STREAM_CODEC, TerrainHighlight::position,
		Shape.STREAM_CODEC, TerrainHighlight::shape,
		Gradient.STREAM_CODEC, TerrainHighlight::color,
		WorldPosition.STREAM_CODEC.optional(FixedWorldPosition.ONE.instance()), TerrainHighlight::scale,
		ByteBufCodecs.VAR_INT, TerrainHighlight::duration,
		TerrainHighlight::new
	);

	public static TerrainHighlight circle(Vec3 position, float radius, Color startColor, Color endColor, int duration) {
		var num = WorldNumber.fixed(1D);
		return new TerrainHighlight(WorldPosition.fixed(position), new CylinderShape(radius, 0F), new PairGradient(startColor, endColor), new DynamicWorldPosition(num, FixedWorldNumber.ONE.instance(), num), duration);
	}
}
