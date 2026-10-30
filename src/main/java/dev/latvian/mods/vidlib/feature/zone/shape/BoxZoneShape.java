package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.registry.DynamicType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public record BoxZoneShape(AABB box) implements ZoneShape {
	public static final DynamicType<RegistryFriendlyByteBuf, ZoneShape> TYPE = DynamicType.create(
		"box",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			MCCodecs.VEC3.fieldOf("start").forGetter(z -> z.box.getMinPosition()),
			MCCodecs.VEC3.fieldOf("end").forGetter(z -> z.box.getMaxPosition())
		).apply(instance, BoxZoneShape::new)),
		CompositeStreamCodec.of(
			MCStreamCodecs.AABB, BoxZoneShape::box,
			BoxZoneShape::new
		)
	);

	public BoxZoneShape(Vec3 start, Vec3 end) {
		this(new AABB(start, end));
	}

	@Override
	public DynamicType<RegistryFriendlyByteBuf, ZoneShape> type() {
		return TYPE;
	}

	@Override
	public AABB toAABB() {
		return box;
	}

	@Override
	public Stream<BlockPos> getBlocks() {
		return BlockPos.betweenClosedStream(box);
	}
}
