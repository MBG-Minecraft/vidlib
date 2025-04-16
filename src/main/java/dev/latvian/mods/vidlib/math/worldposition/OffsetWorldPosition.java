package dev.latvian.mods.vidlib.math.worldposition;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record OffsetWorldPosition(WorldPosition a, WorldPosition b) implements WorldPosition {
	public static final SimpleRegistryType<OffsetWorldPosition> TYPE = SimpleRegistryType.dynamic(VidLib.id("offset"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		WorldPosition.CODEC.fieldOf("a").forGetter(OffsetWorldPosition::a),
		WorldPosition.CODEC.fieldOf("b").forGetter(OffsetWorldPosition::b)
	).apply(instance, OffsetWorldPosition::new)), CompositeStreamCodec.of(
		WorldPosition.STREAM_CODEC, OffsetWorldPosition::a,
		WorldPosition.STREAM_CODEC, OffsetWorldPosition::b,
		OffsetWorldPosition::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var a = this.a.get(ctx);
		var b = this.b.get(ctx);
		return a == null || b == null ? null : new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
}
