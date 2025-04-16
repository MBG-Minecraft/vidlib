package dev.latvian.mods.vidlib.feature.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.ID;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

@AutoInit
public record Location(
	ResourceLocation id,
	ResourceKey<Level> dimension,
	List<BlockPos> positions,
	Vec3 offset,
	double range,
	boolean warp,
	boolean warpRequiresAdmin
) implements Supplier<Vec3> {
	public static final Codec<Location> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("id").forGetter(Location::id),
		VLCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Location::dimension),
		VLCodecs.listOrSelf(BlockPos.CODEC).fieldOf("position").forGetter(Location::positions),
		VLCodecs.VEC_3.optionalFieldOf("offset", KMath.CENTER).forGetter(Location::offset),
		Codec.DOUBLE.optionalFieldOf("range", 0D).forGetter(Location::range),
		Codec.BOOL.optionalFieldOf("warp", true).forGetter(Location::warp),
		Codec.BOOL.optionalFieldOf("warp_requires_admin", true).forGetter(Location::warpRequiresAdmin)
	).apply(instance, Location::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Location> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, Location::id,
		VLStreamCodecs.DIMENSION, Location::dimension,
		BlockPos.STREAM_CODEC.list(), Location::positions,
		VLStreamCodecs.VEC_3, Location::offset,
		ByteBufCodecs.DOUBLE, Location::range,
		ByteBufCodecs.BOOL, Location::warp,
		ByteBufCodecs.BOOL, Location::warpRequiresAdmin,
		Location::new
	);

	public static final VLRegistry<Location> REGISTRY = VLRegistry.createServer("location");
	public static final KnownCodec<Location> KNOWN_CODEC = KnownCodec.of(REGISTRY, Location.class);

	public static class Loader extends JsonRegistryReloadListener<Location> {
		public Loader() {
			super("vidlib/location", DIRECT_CODEC, true, REGISTRY);
		}
	}

	@Override
	public Vec3 get() {
		var pos = positions.getFirst();
		return new Vec3(pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z);
	}

	public Vec3 random(RandomSource source, Vec3 offset) {
		var pos = positions.get(source.nextInt(positions.size()));
		return new Vec3(pos.getX() + offset.x, pos.getY() + offset.y, pos.getZ() + offset.z);
	}

	public Vec3 random(RandomSource source) {
		return random(source, offset);
	}
}
