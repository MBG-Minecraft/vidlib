package dev.beast.mods.shimmer.util.registry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.function.Supplier;

public class VideoResourceLocationArgument implements ArgumentType<ResourceLocation> {
	public static VideoResourceLocationArgument id() {
		return new VideoResourceLocationArgument();
	}

	public static ResourceLocation getId(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, ResourceLocation.class);
	}

	public static SuggestionProvider<CommandSourceStack> registerSuggestionProvider(ResourceLocation registryId, Supplier<Iterable<ResourceLocation>> allIds) {
		return SuggestionProviders.register(registryId, (ctx, builder) -> {
			var input = builder.getRemaining().toLowerCase(Locale.ROOT);
			boolean col = input.indexOf(':') > -1;

			for (var id : allIds.get()) {
				var ids = id.getNamespace().equals("video") ? id.getPath() : id.toString();

				if (col) {
					if (SharedSuggestionProvider.matchesSubStr(input, ids)) {
						builder.suggest(ids);
					}
				} else if (SharedSuggestionProvider.matchesSubStr(input, id.getNamespace()) || id.getNamespace().equals("video") && SharedSuggestionProvider.matchesSubStr(input, id.getPath())) {
					builder.suggest(ids);
				}
			}

			return builder.buildFuture();
		});
	}

	@Override
	public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();

		while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
			reader.skip();
		}

		var s = reader.getString().substring(i, reader.getCursor());

		try {
			return s.indexOf(':') == -1 ? ResourceLocation.fromNamespaceAndPath("video", s) : ResourceLocation.parse(s);
		} catch (ResourceLocationException resourcelocationexception) {
			reader.setCursor(i);
			throw ResourceLocation.ERROR_INVALID.createWithContext(reader);
		}
	}
}

