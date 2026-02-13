package dev.latvian.mods.vidlib.feature.zone.shape;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryEntry;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.feature.zone.ZoneClipResult;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import dev.latvian.mods.vidlib.feature.zone.ZoneLike;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public interface ZoneShape extends ZoneLike, SimpleRegistryEntry {
	SimpleRegistry<ZoneShape> REGISTRY = SimpleRegistry.create(VidLib.id("zone_shape"), c -> PlatformHelper.CURRENT.collectZoneShapes(c));

	Codec<ZoneShape> CODEC = Codec.either(AAIBB.CODEC, REGISTRY.codec()).xmap(either -> either.map(box -> new BlockZoneShape(box.min(), box.max()), Function.identity()), shape -> shape instanceof BlockZoneShape b ? Either.left(b.toAAIBB()) : Either.right(shape));
	StreamCodec<RegistryFriendlyByteBuf, ZoneShape> STREAM_CODEC = ByteBufCodecs.either(AAIBB.STREAM_CODEC, REGISTRY.streamCodec()).map(either -> either.map(box -> new BlockZoneShape(box.min(), box.max()), Function.identity()), shape -> shape instanceof BlockZoneShape b ? Either.left(b.toAAIBB()) : Either.right(shape));

	static void builtinTypes(SimpleRegistryCollector<ZoneShape> registry) {
		registry.register(UniverseZoneShape.TYPE);
		registry.register(ZoneShapeGroup.TYPE);
		registry.register(BlockZoneShape.TYPE);
		registry.register(BoxZoneShape.TYPE);
		registry.register(SphereZoneShape.TYPE);
		registry.register(CylinderZoneShape.TYPE);
		registry.register(RotatedBoxZoneShape.TYPE);
	}

	@Override
	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default ZoneInstance createInstance(ZoneContainer container, Zone zone) {
		return new ZoneInstance(container, zone);
	}

	@Override
	AABB getBoundingBox();

	@Nullable
	default ZoneClipResult clip(ZoneInstance instance, ClipContext ctx) {
		if (contains(ctx.getFrom())) {
			return null;
		}

		var result = AABB.clip(List.of(getBoundingBox()), ctx.getFrom(), ctx.getTo(), BlockPos.ZERO);

		if (result != null && result.getType() == HitResult.Type.BLOCK) {
			return ZoneClipResult.of(instance, this, ctx, new BlockHitResult(result.getLocation(), result.getDirection(), BlockPos.containing(result.getLocation()), false));
		}

		return null;
	}

	default ZoneShape move(double x, double y, double z) {
		var box = getBoundingBox();
		return new BoxZoneShape(box.move(x, y, z));
	}

	default ZoneShape scale(double x, double y, double z) {
		var box = getBoundingBox();
		var c = box.getCenter();
		var sx = box.getXsize() * x / 2D;
		var sy = box.getYsize() * y / 2D;
		var sz = box.getZsize() * z / 2D;
		return new BoxZoneShape(new AABB(c.x - sx, c.y - sy, c.z - sz, c.x + sx, c.y + sy, c.z + sz));
	}
}
