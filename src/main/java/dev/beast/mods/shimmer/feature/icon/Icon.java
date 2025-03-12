package dev.beast.mods.shimmer.feature.icon;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.UV;
import dev.beast.mods.shimmer.util.registry.SimpleRegistry;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface Icon {
	SimpleRegistry<Icon> REGISTRY = SimpleRegistry.create(Icon::type);
	Codec<Icon> CODEC = REGISTRY.valueCodec();
	StreamCodec<RegistryFriendlyByteBuf, Icon> STREAM_CODEC = REGISTRY.valueStreamCodec();
	KnownCodec<Icon> KNOWN_CODEC = KnownCodec.register(Shimmer.id("icon"), CODEC, Icon.class);

	SimpleRegistryType.Unit<Icon> YES = SimpleRegistryType.unit(Shimmer.id("yes"), new TextureIcon(Shimmer.id("textures/misc/yes.png"), UV.FULL, true, Color.WHITE));
	SimpleRegistryType.Unit<Icon> NO = SimpleRegistryType.unit(Shimmer.id("no"), new TextureIcon(Shimmer.id("textures/misc/no.png"), UV.FULL, true, Color.WHITE));

	@AutoInit
	static void bootstrap() {
		REGISTRY.register(EmptyIcon.TYPE);
		REGISTRY.register(ColorIcon.TYPE);
		REGISTRY.register(TextureIcon.TYPE);
		REGISTRY.register(ItemIcon.TYPE);
		REGISTRY.register(AtlasSpriteIcon.TYPE);

		REGISTRY.register(YES);
		REGISTRY.register(NO);
	}

	default SimpleRegistryType<?> type() {
		return REGISTRY.getType(this);
	}

	default IconHolder holder() {
		return new IconHolder(this);
	}
}
