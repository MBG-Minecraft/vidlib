package dev.beast.mods.shimmer.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public record BoxZoneShape(AABB box) implements ZoneShape {
	public static final SimpleRegistryType<BoxZoneShape> TYPE = SimpleRegistryType.dynamic(Shimmer.id("box"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		ShimmerCodecs.VEC_3.fieldOf("start").forGetter(z -> z.box.getMinPosition()),
		ShimmerCodecs.VEC_3.fieldOf("end").forGetter(z -> z.box.getMaxPosition())
	).apply(instance, (start, end) -> new BoxZoneShape(new AABB(start, end)))), CompositeStreamCodec.of(
		ShimmerStreamCodecs.AABB,
		BoxZoneShape::box,
		BoxZoneShape::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(box);
	}

	@Override
	public void writeUUID(FriendlyByteBuf buf) {
		buf.writeUtf(type().id().toString());
		buf.writeDouble(box.minX);
		buf.writeDouble(box.minY);
		buf.writeDouble(box.minZ);
		buf.writeDouble(box.maxX);
		buf.writeDouble(box.maxY);
		buf.writeDouble(box.maxZ);
	}
}
