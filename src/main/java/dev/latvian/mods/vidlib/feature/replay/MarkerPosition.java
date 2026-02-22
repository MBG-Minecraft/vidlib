package dev.latvian.mods.vidlib.feature.replay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.math.KMath;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record MarkerPosition(Vec3 position, ResourceKey<Level> dimension) {
	public static final Codec<MarkerPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.DOUBLE.fieldOf("x").forGetter(p -> p.position.x),
		Codec.DOUBLE.fieldOf("y").forGetter(p -> p.position.y),
		Codec.DOUBLE.fieldOf("z").forGetter(p -> p.position.z),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(MarkerPosition::dimension)
	).apply(instance, MarkerPosition::new));

	public MarkerPosition(double x, double y, double z, ResourceKey<Level> dimension) {
		this(KMath.vec3(x, y, z), dimension);
	}
}
