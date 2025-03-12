package dev.beast.mods.shimmer.feature.codec;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AutoInit
public record KnownCodec<T>(ResourceLocation id, Codec<T> codec, Codec<Optional<T>> optionalCodec, Class<T> type) {
	public static final Map<ResourceLocation, KnownCodec<?>> MAP = new HashMap<>();

	public static <T> KnownCodec<T> register(ResourceLocation id, Codec<T> codec, Class<T> type) {
		var knownCodec = new KnownCodec<>(id, codec, ShimmerCodecs.optional(codec), type);
		MAP.put(id, knownCodec);
		return knownCodec;
	}

	public CodecArgument<T> argument(CommandBuildContext commandBuildContext) {
		return new CodecArgument<>(commandBuildContext.createSerializationContext(NbtOps.INSTANCE), this);
	}

	public OptionalCodecArgument<T> optionalArgument(CommandBuildContext commandBuildContext) {
		return new OptionalCodecArgument<>(commandBuildContext.createSerializationContext(NbtOps.INSTANCE), this);
	}

	public <S> T get(CommandContext<S> context, String name) {
		return context.getArgument(name, type);
	}

	@SuppressWarnings("unchecked")
	public <S> Optional<T> getOptional(CommandContext<S> context, String name) {
		return context.getArgument(name, Optional.class);
	}
}
