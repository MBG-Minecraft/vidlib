package dev.latvian.mods.vidlib.feature.highlight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.color.Color;
import dev.latvian.mods.kmath.color.Gradient;
import dev.latvian.mods.kmath.color.PairGradient;
import dev.latvian.mods.kmath.shape.CylinderShape;
import dev.latvian.mods.kmath.shape.Shape;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.codec.RegisteredDataType;
import dev.latvian.mods.vidlib.math.worldnumber.FixedWorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldvector.DynamicWorldVector;
import dev.latvian.mods.vidlib.math.worldvector.FixedWorldVector;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

@AutoInit
public record TerrainHighlight(
	WorldVector position,
	Shape shape,
	Gradient color,
	WorldVector scale,
	int duration
) {
	public static final Codec<TerrainHighlight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WorldVector.CODEC.fieldOf("position").forGetter(TerrainHighlight::position),
		Shape.CODEC.fieldOf("shape").forGetter(TerrainHighlight::shape),
		Gradient.CODEC.fieldOf("color").forGetter(TerrainHighlight::color),
		WorldVector.CODEC.optionalFieldOf("scale", FixedWorldVector.ONE.instance()).forGetter(TerrainHighlight::scale),
		Codec.INT.optionalFieldOf("duration", 20).forGetter(TerrainHighlight::duration)
	).apply(instance, TerrainHighlight::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TerrainHighlight> STREAM_CODEC = CompositeStreamCodec.of(
		WorldVector.STREAM_CODEC, TerrainHighlight::position,
		Shape.STREAM_CODEC, TerrainHighlight::shape,
		Gradient.STREAM_CODEC, TerrainHighlight::color,
		WorldVector.STREAM_CODEC.optional(FixedWorldVector.ONE.instance()), TerrainHighlight::scale,
		ByteBufCodecs.VAR_INT, TerrainHighlight::duration,
		TerrainHighlight::new
	);

	public static final DataType<TerrainHighlight> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, TerrainHighlight.class);
	public static final RegisteredDataType<TerrainHighlight> REGISTERED_DATA_TYPE = RegisteredDataType.register(VidLib.id("terrain_highlight"), DATA_TYPE);

	public static TerrainHighlight circle(Vec3 position, float radius, Color startColor, Color endColor, int duration) {
		var num = WorldNumber.fixed(1D);
		return new TerrainHighlight(WorldVector.fixed(position), new CylinderShape(radius, 0F), new PairGradient(startColor, endColor), new DynamicWorldVector(num, FixedWorldNumber.ONE.instance(), num), duration);
	}
}
