package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryType;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Optional;

public record Clock(
	List<ClockLocation> locations,
	Optional<ScreenClock> screen
) implements CustomRegistryValue<RegistryFriendlyByteBuf, Clock> {
	public static final DynamicType<RegistryFriendlyByteBuf, Clock> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			ClockLocation.CODEC.listOf().optionalFieldOf("locations", List.of()).forGetter(Clock::locations),
			ScreenClock.CODEC.optionalFieldOf("screen").forGetter(Clock::screen)
		).apply(instance, Clock::new)),
		CompositeStreamCodec.of(
			KLibStreamCodecs.listOf(ClockLocation.STREAM_CODEC), Clock::locations,
			ByteBufCodecs.optional(ScreenClock.STREAM_CODEC), Clock::screen,
			Clock::new
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, Clock> REGISTRY = CustomRegistry.createNoValueSync("clock", TYPE);
	public static final Codec<Ref<Clock>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<Clock>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<Clock>> DATA_TYPE = REGISTRY.dataType();

	public static class ServerLoader extends JsonRegistryReloadListener<Clock> {
		public ServerLoader() {
			super("vidlib/clock", REGISTRY);
		}
	}

	@Override
	public CustomRegistry<RegistryFriendlyByteBuf, Clock> getRegistry() {
		return REGISTRY;
	}

	@Override
	public CustomRegistryType<RegistryFriendlyByteBuf, Clock> type() {
		return TYPE;
	}
}
