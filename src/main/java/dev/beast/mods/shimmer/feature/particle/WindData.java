package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@AutoInit
public record WindData(
	WindParticleOptions options,
	WindType type,
	BlockPos position,
	int count,
	float radius,
	float yaw
) {
	public static final Codec<WindData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		WindParticleOptions.CODEC.fieldOf("options").forGetter(WindData::options),
		WindType.KNOWN_CODEC.codec().optionalFieldOf("type", WindType.CIRCULAR).forGetter(WindData::type),
		BlockPos.CODEC.fieldOf("position").forGetter(WindData::position),
		Codec.INT.optionalFieldOf("count", 1).forGetter(WindData::count),
		Codec.FLOAT.optionalFieldOf("radius", 20F).forGetter(WindData::radius),
		Codec.FLOAT.optionalFieldOf("yaw", 0F).forGetter(WindData::yaw)
	).apply(instance, WindData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, WindData> STREAM_CODEC = CompositeStreamCodec.of(
		WindParticleOptions.STREAM_CODEC, WindData::options,
		WindType.KNOWN_CODEC.streamCodec().optional(WindType.CIRCULAR), WindData::type,
		BlockPos.STREAM_CODEC, WindData::position,
		ByteBufCodecs.VAR_INT, WindData::count,
		ByteBufCodecs.FLOAT, WindData::radius,
		ByteBufCodecs.FLOAT, WindData::yaw,
		WindData::new
	);

	public static final KnownCodec<WindData> KNOWN_CODEC = KnownCodec.register(Shimmer.id("wind"), CODEC, STREAM_CODEC, WindData.class);
}
