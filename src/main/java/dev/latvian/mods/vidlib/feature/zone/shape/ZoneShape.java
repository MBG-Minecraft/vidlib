package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.RefOptimizer;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import dev.latvian.mods.vidlib.feature.zone.ZoneLike;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ZoneShape extends ZoneLike, RefOptimizer<ZoneShape> {
	CustomRegistry<ByteBuf, ZoneShape> REGISTRY = CustomRegistry.<ByteBuf, ZoneShape>builder()
		.keys(ID.vidlib("zone_shape"), VidLib.ID)
		.type(ZoneShape::type)
		.customCodec(AAIBB.CODEC.flatComapMap(box -> BlockZoneShape.of(box.min(), box.max()), shape -> {
			if (shape instanceof BlockZoneShape b) {
				return DataResult.success(b.intBox());
			} else {
				return DataResult.error(() -> "Not an AAIBB");
			}
		}))
		.build();

	Codec<Ref<ZoneShape>> CODEC = REGISTRY.codec();
	StreamCodec<ByteBuf, Ref<ZoneShape>> STREAM_CODEC = REGISTRY.streamCodec();
	DataType<Ref<ZoneShape>> DATA_TYPE = REGISTRY.dataType();

	static void builtInTypes(CustomRegistryTypeCollector<ByteBuf, ZoneShape> registry) {
		registry.register(UniverseZoneShape.TYPE);
		registry.register(ZoneShapeGroup.TYPE);
		registry.register(BlockZoneShape.TYPE);
		registry.register(BoxZoneShape.TYPE);
		registry.register(SphereZoneShape.TYPE);
		registry.register(CylinderZoneShape.TYPE);
		registry.register(RotatedBoxZoneShape.TYPE);
	}

	@Nullable
	default CustomRegistryType<ByteBuf, ZoneShape> type() {
		return null;
	}

	default ZoneInstance createInstance(ZoneContainer container, Zone zone) {
		return new ZoneInstance(container, zone);
	}

	@Override
	AABB toAABB();

	@Nullable
	default ZoneClipResult clip(ZoneInstance instance, ClipContext ctx) {
		if (contains(ctx.getFrom())) {
			return null;
		}

		var result = AABB.clip(List.of(toAABB()), ctx.getFrom(), ctx.getTo(), BlockPos.ZERO);

		if (result != null && result.getType() == HitResult.Type.BLOCK) {
			return ZoneClipResult.of(instance, this, ctx, new BlockHitResult(result.getLocation(), result.getDirection(), BlockPos.containing(result.getLocation()), false));
		}

		return null;
	}

	default ZoneShape move(double x, double y, double z) {
		var box = toAABB();
		return new BoxZoneShape(box.move(x, y, z));
	}

	default ZoneShape scale(double x, double y, double z) {
		var box = toAABB();
		var c = box.getCenter();
		var sx = box.getXsize() * x / 2D;
		var sy = box.getYsize() * y / 2D;
		var sz = box.getZsize() * z / 2D;
		return new BoxZoneShape(new AABB(c.x - sx, c.y - sy, c.z - sz, c.x + sx, c.y + sy, c.z + sz));
	}
}
