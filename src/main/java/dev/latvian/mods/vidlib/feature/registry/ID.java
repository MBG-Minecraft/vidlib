package dev.latvian.mods.vidlib.feature.registry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.util.Cast;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface ID {
	Codec<ResourceLocation> CODEC = Codec.STRING.xmap(ID::idFromString, ID::idToString);
	StreamCodec<ByteBuf, ResourceLocation> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ID::idFromString, ID::idToString);
	StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> REGISTRY_STREAM_CODEC = Cast.to(STREAM_CODEC);

	static ResourceLocation mc(String path) {
		return ResourceLocation.withDefaultNamespace(path);
	}

	static ResourceLocation idFromString(String string) {
		return string.indexOf(':') == -1 ? ResourceLocation.withDefaultNamespace(string) : ResourceLocation.parse(string);
	}

	static String idToString(ResourceLocation id) {
		return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
	}

	static ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();

		while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
			reader.skip();
		}

		var s = reader.getString().substring(i, reader.getCursor());

		try {
			return idFromString(s);
		} catch (ResourceLocationException resourcelocationexception) {
			reader.setCursor(i);
			throw ResourceLocation.ERROR_INVALID.createWithContext(reader);
		}
	}

	static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, Supplier<Iterable<ResourceLocation>> allIds) {
		var input = builder.getRemaining().toLowerCase(Locale.ROOT);
		boolean col = input.indexOf(':') > -1;

		for (var id : allIds.get()) {
			var ids = idToString(id);

			if (col) {
				if (SharedSuggestionProvider.matchesSubStr(input, ids)) {
					builder.suggest(ids);
				}
			} else if (SharedSuggestionProvider.matchesSubStr(input, id.getNamespace()) || id.getNamespace().equals("minecraft") && SharedSuggestionProvider.matchesSubStr(input, id.getPath())) {
				builder.suggest(ids);
			}
		}

		return builder.buildFuture();
	}

	static SuggestionProvider<CommandSourceStack> registerSuggestionProvider(ResourceLocation id, Supplier<Iterable<ResourceLocation>> allIds) {
		return SuggestionProviders.register(id, (ctx, builder) -> suggest(builder, allIds));
	}
}
