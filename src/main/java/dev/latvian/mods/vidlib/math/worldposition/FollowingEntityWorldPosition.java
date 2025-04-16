package dev.latvian.mods.vidlib.math.worldposition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record FollowingEntityWorldPosition(Either<Integer, UUID> entityId, EntityPositionType positionType) implements WorldPosition {
	public static final SimpleRegistryType<FollowingEntityWorldPosition> TYPE = SimpleRegistryType.dynamic(VidLib.id("following_entity"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.either(Codec.INT, VLCodecs.UUID).fieldOf("entity_id").forGetter(FollowingEntityWorldPosition::entityId),
		EntityPositionType.CODEC.optionalFieldOf("position_type", EntityPositionType.CENTER).forGetter(FollowingEntityWorldPosition::positionType)
	).apply(instance, FollowingEntityWorldPosition::new)), CompositeStreamCodec.of(
		ByteBufCodecs.either(ByteBufCodecs.VAR_INT, VLStreamCodecs.UUID), FollowingEntityWorldPosition::entityId,
		EntityPositionType.STREAM_CODEC, FollowingEntityWorldPosition::positionType,
		FollowingEntityWorldPosition::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var entity = ctx.level.getEntityByEither(entityId);
		return entity == null ? null : entity.getPosition(positionType);
	}
}
