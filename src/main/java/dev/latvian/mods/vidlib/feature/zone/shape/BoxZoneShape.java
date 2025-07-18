package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public record BoxZoneShape(AABB box) implements ZoneShape {
	public static final SimpleRegistryType<BoxZoneShape> TYPE = SimpleRegistryType.dynamic("box", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MCCodecs.VEC3.fieldOf("start").forGetter(z -> z.box.getMinPosition()),
		MCCodecs.VEC3.fieldOf("end").forGetter(z -> z.box.getMaxPosition())
	).apply(instance, (start, end) -> new BoxZoneShape(new AABB(start, end)))), CompositeStreamCodec.of(
		MCStreamCodecs.AABB,
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
}
