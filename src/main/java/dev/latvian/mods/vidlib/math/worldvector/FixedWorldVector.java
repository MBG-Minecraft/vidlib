package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;

public record FixedWorldVector(Vec3 pos) implements WorldVector {
	public static final SimpleRegistryType.Unit<FixedWorldVector> ZERO = SimpleRegistryType.unit("zero", new FixedWorldVector(Vec3.ZERO));
	public static final SimpleRegistryType.Unit<FixedWorldVector> ONE = SimpleRegistryType.unit("one", new FixedWorldVector(new Vec3(1D, 1D, 1D)));

	public static final SimpleRegistryType<FixedWorldVector> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.VEC3.fieldOf("pos").forGetter(FixedWorldVector::pos)
	).apply(instance, FixedWorldVector::new)), MCStreamCodecs.VEC3.map(FixedWorldVector::new, FixedWorldVector::pos));

	@Override
	public SimpleRegistryType<?> type() {
		return pos.x == 0D && pos.y == 0D && pos.z == 0D ? ZERO : pos.x == 1D && pos.y == 1D && pos.z == 1D ? ONE : TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		return pos;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
