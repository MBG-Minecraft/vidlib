package dev.latvian.mods.vidlib.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;

public record FixedWorldPosition(Vec3 pos) implements WorldPosition {
	public static final SimpleRegistryType.Unit<FixedWorldPosition> ZERO = SimpleRegistryType.unit("zero", new FixedWorldPosition(Vec3.ZERO));
	public static final SimpleRegistryType.Unit<FixedWorldPosition> ONE = SimpleRegistryType.unit("one", new FixedWorldPosition(new Vec3(1D, 1D, 1D)));

	public static final SimpleRegistryType<FixedWorldPosition> TYPE = SimpleRegistryType.dynamic("fixed", RecordCodecBuilder.mapCodec(instance -> instance.group(
		VLCodecs.VEC_3.fieldOf("pos").forGetter(FixedWorldPosition::pos)
	).apply(instance, FixedWorldPosition::new)), VLStreamCodecs.VEC_3.map(FixedWorldPosition::new, FixedWorldPosition::pos));

	@Override
	public SimpleRegistryType<?> type() {
		return pos.x == 0D && pos.y == 0D && pos.z == 0D ? ZERO : pos.x == 1D && pos.y == 1D && pos.z == 1D ? ONE : TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		return pos;
	}
}
