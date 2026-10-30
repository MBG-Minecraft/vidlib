package dev.latvian.mods.vidlib.math.kvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.entity.PositionType;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record FollowingPropKVector(int prop, PositionType positionType) implements KVector {
	public static final DynamicType<RegistryFriendlyByteBuf, KVector> TYPE = DynamicType.create(
		"following_prop",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("prop").forGetter(FollowingPropKVector::prop),
			PositionType.CODEC.optionalFieldOf("position_type", PositionType.CENTER).forGetter(FollowingPropKVector::positionType)
		).apply(instance, FollowingPropKVector::new)),
		CompositeStreamCodec.of(
			ByteBufCodecs.VAR_INT, FollowingPropKVector::prop,
			PositionType.STREAM_CODEC, FollowingPropKVector::positionType,
			FollowingPropKVector::new
		)
	);

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, KVector> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(KNumberContext ctx) {
		var props = VLPlatformHelper.CURRENT.getProps(ctx.level);
		var p = props.levelProps.get(prop);
		return p == null ? null : p.getPos(positionType);
	}
}
