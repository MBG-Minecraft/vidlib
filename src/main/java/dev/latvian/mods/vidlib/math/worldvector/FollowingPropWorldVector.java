package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingPropWorldVector(int prop, EntityPositionType positionType) implements WorldVector {
	public static final SimpleRegistryType<FollowingPropWorldVector> TYPE = SimpleRegistryType.dynamic("following_prop", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.INT.fieldOf("prop").forGetter(FollowingPropWorldVector::prop),
		EntityPositionType.CODEC.optionalFieldOf("position_type", EntityPositionType.CENTER).forGetter(FollowingPropWorldVector::positionType)
	).apply(instance, FollowingPropWorldVector::new)), CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, FollowingPropWorldVector::prop,
		EntityPositionType.STREAM_CODEC, FollowingPropWorldVector::positionType,
		FollowingPropWorldVector::new
	));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var p = ctx.level.getProps().levelProps.get(prop);
		return p == null ? null : p.getPos(positionType);
	}
}
