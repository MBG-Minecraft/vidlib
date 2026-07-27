package dev.latvian.mods.vidlib.feature.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.kvector.KVector;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

@AutoInit
public record Location(
	Ref<Location> ref,
	ResourceKey<Level> dimension,
	List<Ref<KVector>> positions,
	double range,
	boolean warp,
	boolean warpRequiresAdmin
) implements CustomRegistryValue<RegistryFriendlyByteBuf, Location>, Supplier<Ref<KVector>> {
	public static final DynamicType<RegistryFriendlyByteBuf, Location> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ref.<Location>contextRefCodec().forGetter(Location::ref),
			MCCodecs.DIMENSION.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(Location::dimension),
			KLibCodecs.listOrSelf(KVector.CODEC).fieldOf("position").forGetter(Location::positions),
			Codec.DOUBLE.optionalFieldOf("range", 0D).forGetter(Location::range),
			Codec.BOOL.optionalFieldOf("warp", true).forGetter(Location::warp),
			Codec.BOOL.optionalFieldOf("warp_requires_admin", true).forGetter(Location::warpRequiresAdmin)
		).apply(instance, Location::new)),
		CompositeStreamCodec.of(
			Ref.contextRefStreamCodec(), Location::ref,
			MCStreamCodecs.DIMENSION, Location::dimension,
			KLibStreamCodecs.listOf(KVector.STREAM_CODEC), Location::positions,
			ByteBufCodecs.DOUBLE, Location::range,
			ByteBufCodecs.BOOL, Location::warp,
			ByteBufCodecs.BOOL, Location::warpRequiresAdmin,
			Location::new
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, Location> REGISTRY = CustomRegistry.create("location", TYPE);
	public static final Codec<Ref<Location>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<Location>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<Location>> DATA_TYPE = REGISTRY.dataType();

	public static class ServerLoader extends JsonRegistryReloadListener<Location> {
		public ServerLoader() {
			super("vidlib/location", REGISTRY);
		}
	}

	@Override
	public CustomRegistry<RegistryFriendlyByteBuf, Location> getRegistry() {
		return REGISTRY;
	}

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, Location> type() {
		return TYPE;
	}

	@Override
	public Ref<KVector> get() {
		return positions.getFirst();
	}

	public Ref<KVector> sample(RandomSource source) {
		return positions.get(source.nextInt(positions.size()));
	}
}
