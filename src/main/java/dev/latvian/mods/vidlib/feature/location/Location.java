package dev.latvian.mods.vidlib.feature.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

@AutoInit
public record Location(
	ResourceLocation id,
	ResourceKey<Level> dimension,
	List<KVector> positions,
	double range,
	boolean warp,
	boolean warpRequiresAdmin
) implements Supplier<KVector> {
	public static final Codec<Location> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("id").forGetter(Location::id),
		MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Location::dimension),
		KLibCodecs.listOrSelf(KVector.CODEC).fieldOf("position").forGetter(Location::positions),
		Codec.DOUBLE.optionalFieldOf("range", 0D).forGetter(Location::range),
		Codec.BOOL.optionalFieldOf("warp", true).forGetter(Location::warp),
		Codec.BOOL.optionalFieldOf("warp_requires_admin", true).forGetter(Location::warpRequiresAdmin)
	).apply(instance, Location::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Location> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, Location::id,
		MCStreamCodecs.DIMENSION, Location::dimension,
		KLibStreamCodecs.listOf(KVector.STREAM_CODEC), Location::positions,
		ByteBufCodecs.DOUBLE, Location::range,
		ByteBufCodecs.BOOL, Location::warp,
		ByteBufCodecs.BOOL, Location::warpRequiresAdmin,
		Location::new
	);

	public static final VLRegistry<Location> REGISTRY = VLRegistry.createServer("location", Location.class);
	public static final DataType<Location> DATA_TYPE = REGISTRY.dataType();
	public static final CommandDataType<Location> COMMAND = CommandDataType.of(DATA_TYPE);

	public static class Loader extends JsonRegistryReloadListener<Location> {
		public Loader(VLRegistry<Location> registry) {
			super("vidlib/location", DIRECT_CODEC, true, registry);
		}
	}

	@Override
	public KVector get() {
		return positions.getFirst();
	}

	public KVector random(RandomSource source) {
		return positions.get(source.nextInt(positions.size()));
	}
}
