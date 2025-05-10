package dev.latvian.mods.vidlib.feature.location;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.codec.KnownCodec;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.feature.registry.ID;
import dev.latvian.mods.vidlib.feature.registry.RegistryRef;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.math.worldposition.WorldPosition;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@AutoInit
public record Location(
	ResourceLocation id,
	ResourceKey<Level> dimension,
	List<WorldPosition> positions,
	double range,
	boolean warp,
	boolean warpRequiresAdmin
) implements Supplier<WorldPosition> {
	public static final Codec<Location> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("id").forGetter(Location::id),
		VLCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Location::dimension),
		VLCodecs.listOrSelf(WorldPosition.CODEC).fieldOf("position").forGetter(Location::positions),
		Codec.DOUBLE.optionalFieldOf("range", 0D).forGetter(Location::range),
		Codec.BOOL.optionalFieldOf("warp", true).forGetter(Location::warp),
		Codec.BOOL.optionalFieldOf("warp_requires_admin", true).forGetter(Location::warpRequiresAdmin)
	).apply(instance, Location::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Location> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, Location::id,
		VLStreamCodecs.DIMENSION, Location::dimension,
		WorldPosition.STREAM_CODEC.list(), Location::positions,
		ByteBufCodecs.DOUBLE, Location::range,
		ByteBufCodecs.BOOL, Location::warp,
		ByteBufCodecs.BOOL, Location::warpRequiresAdmin,
		Location::new
	);

	public static final VLRegistry<Location> REGISTRY = VLRegistry.createServer("location");
	public static final VLRegistry<Location> CLIENT_REGISTRY = VLRegistry.createClient("location");
	public static final Codec<RegistryRef<Location>> CLIENT_REF_CODEC = Codec.either(CLIENT_REGISTRY.refCodec(), REGISTRY.refCodec()).xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);

	public static final Codec<RegistryRef<Location>> CLIENT_CODEC = Codec.either(CLIENT_REF_CODEC, DIRECT_CODEC).xmap(
		either -> either.map(Function.identity(), loc -> new RegistryRef<>(null, loc)),
		ref -> ref.id() != null ? Either.left(ref) : Either.right(ref.get())
	);

	public static final KnownCodec<Location> KNOWN_CODEC = KnownCodec.of(REGISTRY, Location.class);

	public static class Loader extends JsonRegistryReloadListener<Location> {
		public Loader(VLRegistry<Location> registry) {
			super("vidlib/location", DIRECT_CODEC, true, registry);
		}
	}

	@Override
	public WorldPosition get() {
		return positions.getFirst();
	}

	public WorldPosition random(RandomSource source) {
		return positions.get(source.nextInt(positions.size()));
	}
}
