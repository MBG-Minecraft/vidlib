package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record BoxZoneShape(AABB box) implements ZoneShape {
	public static final ZoneShapeType<BoxZoneShape> TYPE = new ZoneShapeType<>("box", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3.CODEC.fieldOf("start").forGetter(z -> z.box.getMinPosition()),
		Vec3.CODEC.fieldOf("end").forGetter(z -> z.box.getMaxPosition())
	).apply(instance, (start, end) -> new BoxZoneShape(new AABB(start, end)))), StreamCodec.composite(
		ByteBufCodecs.DOUBLE,
		z -> z.box.minX,
		ByteBufCodecs.DOUBLE,
		z -> z.box.minY,
		ByteBufCodecs.DOUBLE,
		z -> z.box.minZ,
		ByteBufCodecs.DOUBLE,
		z -> z.box.maxX,
		ByteBufCodecs.DOUBLE,
		z -> z.box.maxY,
		ByteBufCodecs.DOUBLE,
		z -> z.box.maxZ,
		(minX, minY, minZ, maxX, maxY, maxZ) -> new BoxZoneShape(new AABB(minX, minY, minZ, maxX, maxY, maxZ))
	));

	@Override
	public ZoneShapeType<?> type() {
		return TYPE;
	}

	@Override
	public AABB getBoundingBox() {
		return box;
	}
}
