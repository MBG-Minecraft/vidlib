package dev.beast.mods.shimmer.feature.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.function.Supplier;

@AutoInit
public record Location(
	ResourceLocation id,
	ResourceKey<Level> dimension,
	BlockPos position,
	Vec3 offset,
	double range,
	boolean warpRequiresAdmin
) implements Supplier<Vec3> {
	public static final Codec<Location> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ShimmerCodecs.VIDEO_ID.fieldOf("id").forGetter(Location::id),
		ShimmerCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Location::dimension),
		BlockPos.CODEC.fieldOf("position").forGetter(Location::position),
		ShimmerCodecs.VEC_3.optionalFieldOf("offset", KMath.CENTER).forGetter(Location::offset),
		Codec.DOUBLE.optionalFieldOf("range", 0D).forGetter(Location::range),
		Codec.BOOL.optionalFieldOf("warp_requires_admin", true).forGetter(Location::warpRequiresAdmin)
	).apply(instance, Location::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Location> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ShimmerStreamCodecs.VIDEO_ID, Location::id,
		ShimmerStreamCodecs.DIMENSION, Location::dimension,
		BlockPos.STREAM_CODEC, Location::position,
		ShimmerStreamCodecs.VEC_3, Location::offset,
		ByteBufCodecs.DOUBLE, Location::range,
		ByteBufCodecs.BOOL, Location::warpRequiresAdmin,
		Location::new
	);

	public static final RegistryReference.IdHolder<Location> REGISTRY = RegistryReference.createServerIdHolder("location", false);
	public static final KnownCodec<Location> KNOWN_CODEC = KnownCodec.register(REGISTRY, Location.class);

	public static class Loader extends JsonCodecReloadListener<Location> {
		public Loader() {
			super("shimmer/location", DIRECT_CODEC, true);
		}

		@Override
		protected void apply(Map<ResourceLocation, Location> from) {
			REGISTRY.update(Map.copyOf(from));
		}
	}

	@Override
	public Vec3 get() {
		return new Vec3(position.getX() + offset.x, position.getY() + offset.y, position.getZ() + offset.z);
	}
}
