package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipContext;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneLike;
import dev.latvian.mods.vidlib.feature.zone.ZoneVolume;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ZoneShape extends ZoneLike, CustomRegistryValue<ByteBuf, ZoneShape> {
	CustomRegistry<ByteBuf, ZoneShape> REGISTRY = CustomRegistry.<ByteBuf, ZoneShape>builder("zone_shape")
		.customCodec(AAIBB.CODEC.flatComapMap(BlockZoneShape::of, shape -> {
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
		registry.register(JoinedZoneShape.TYPE);
		registry.register(BlockZoneShape.TYPE);
		registry.register(BoxZoneShape.TYPE);
		registry.register(SphereZoneShape.TYPE);
		registry.register(CylinderZoneShape.TYPE);
		registry.register(RotatedBoxZoneShape.TYPE);
	}

	@Override
	default CustomRegistry<ByteBuf, ZoneShape> getRegistry() {
		return REGISTRY;
	}

	default Zone createInstance(ZoneContainer container, ZoneVolume zone) {
		return new Zone(container, zone);
	}

	@Override
	AABB toAABB();

	@Nullable
	default ZoneClipResult clip(ZoneClipContext ctx) {
		if (contains(ctx.getFrom())) {
			return null;
		}

		var result = AABB.clip(List.of(toAABB()), ctx.getFrom(), ctx.getTo(), BlockPos.ZERO);

		if (result != null && result.getType() == HitResult.Type.BLOCK) {
			return ZoneClipResult.of(ctx, new BlockHitResult(result.getLocation(), result.getDirection(), BlockPos.containing(result.getLocation()), false));
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
