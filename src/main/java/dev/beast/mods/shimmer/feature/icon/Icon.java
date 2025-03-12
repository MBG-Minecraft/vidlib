package dev.beast.mods.shimmer.feature.icon;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Icon {
	SimpleRegistry<Icon> REGISTRY = SimpleRegistry.create(Icon::type);
	Codec<Icon> CODEC = REGISTRY.valueCodec();
	StreamCodec<RegistryFriendlyByteBuf, Icon> STREAM_CODEC = REGISTRY.valueStreamCodec();
	KnownCodec<Icon> KNOWN_CODEC = KnownCodec.register(Shimmer.id("icon"), CODEC, Icon.class);

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(EmptyIcon.TYPE);
		REGISTRY.register(ColorIcon.TYPE);
		REGISTRY.register(TextureIcon.TYPE);
		REGISTRY.register(ItemIcon.TYPE);
		REGISTRY.register(AtlasSpriteIcon.TYPE);

		REGISTRY.register(AtlasSpriteIcon.YES);
		REGISTRY.register(AtlasSpriteIcon.NO);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default IconHolder holder() {
		return new IconHolder(this);
	}
}
