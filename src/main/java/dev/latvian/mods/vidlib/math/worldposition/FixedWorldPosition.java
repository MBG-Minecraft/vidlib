package dev.latvian.mods.vidlib.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;

public record FixedWorldPosition(Vec3 pos) implements WorldPosition {
	public static final SimpleRegistryType.Unit<FixedWorldPosition> ZERO = SimpleRegistryType.unit(VidLib.id("zero"), new FixedWorldPosition(Vec3.ZERO));

	public static final SimpleRegistryType<FixedWorldPosition> TYPE = SimpleRegistryType.dynamic(VidLib.id("fixed"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		VLCodecs.VEC_3.fieldOf("pos").forGetter(FixedWorldPosition::pos)
	).apply(instance, FixedWorldPosition::new)), VLStreamCodecs.VEC_3.map(FixedWorldPosition::new, FixedWorldPosition::pos));

	@Override
	public SimpleRegistryType<?> type() {
		return pos == Vec3.ZERO ? ZERO : TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		return pos;
	}
}
