package dev.beast.mods.shimmer.feature.prop;

import com.mojang.serialization.MapCodec;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import dev.beast.mods.shimmer.util.Lazy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record PropType<P extends Prop>(
	ResourceLocation id,
	MapCodec<P> mapCodec,
	StreamCodec<? super RegistryFriendlyByteBuf, P> streamCodec
) {
	public static final Lazy<Map<ResourceLocation, PropType<?>>> ALL = Lazy.of(() -> {
		var map = new HashMap<ResourceLocation, PropType<?>>();

		for (var s : AutoRegister.SCANNED.get()) {
			if (s.value() instanceof PropType<?> propType) {
				map.put(propType.id, propType);
			}
		}

		return Map.copyOf(map);
	});
}
