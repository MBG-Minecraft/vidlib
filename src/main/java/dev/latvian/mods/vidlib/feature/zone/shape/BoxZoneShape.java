package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public record BoxZoneShape(AABB box) implements ZoneShape {
	public static final SimpleRegistryType<BoxZoneShape> TYPE = SimpleRegistryType.dynamic("box", RecordCodecBuilder.mapCodec(instance -> instance.group(
		VLCodecs.VEC_3.fieldOf("start").forGetter(z -> z.box.getMinPosition()),
		VLCodecs.VEC_3.fieldOf("end").forGetter(z -> z.box.getMaxPosition())
	).apply(instance, (start, end) -> new BoxZoneShape(new AABB(start, end)))), CompositeStreamCodec.of(
		VLStreamCodecs.AABB,
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
		buf.writeUtf(type().id());
		buf.writeDouble(box.minX);
		buf.writeDouble(box.minY);
		buf.writeDouble(box.minZ);
		buf.writeDouble(box.maxX);
		buf.writeDouble(box.maxY);
		buf.writeDouble(box.maxZ);
	}
}
