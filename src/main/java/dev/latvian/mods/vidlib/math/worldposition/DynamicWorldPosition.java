package dev.latvian.mods.vidlib.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumber;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;

public record DynamicWorldPosition(WorldNumber x, WorldNumber y, WorldNumber z) implements WorldPosition {
	public static final SimpleRegistryType<DynamicWorldPosition> TYPE = SimpleRegistryType.dynamic("dynamic", RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldNumber.CODEC.fieldOf("x").forGetter(DynamicWorldPosition::x),
		WorldNumber.CODEC.fieldOf("y").forGetter(DynamicWorldPosition::y),
		WorldNumber.CODEC.fieldOf("z").forGetter(DynamicWorldPosition::z)
	).apply(instance, DynamicWorldPosition::new)), CompositeStreamCodec.of(
		WorldNumber.STREAM_CODEC, DynamicWorldPosition::x,
		WorldNumber.STREAM_CODEC, DynamicWorldPosition::y,
		WorldNumber.STREAM_CODEC, DynamicWorldPosition::z,
		DynamicWorldPosition::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		return new Vec3(x.get(ctx), y.get(ctx), z.get(ctx));
	}
}
