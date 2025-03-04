package dev.beast.mods.shimmer.feature.session;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record CookieType<T extends Cookie>(ResourceLocation id, Supplier<Cookie> factory, @Nullable MapCodec<T> mapCodec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
	public CookieType(ResourceLocation id, Supplier<Cookie> factory) {
		this(id, factory, null, null);
	}

	public CookieType(ResourceLocation id, Supplier<Cookie> factory, @Nullable MapCodec<T> mapCodec) {
		this(id, factory, mapCodec, null);
	}

	public CookieType(ResourceLocation id, Supplier<Cookie> factory, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		this(id, factory, null, streamCodec);
	}
}
