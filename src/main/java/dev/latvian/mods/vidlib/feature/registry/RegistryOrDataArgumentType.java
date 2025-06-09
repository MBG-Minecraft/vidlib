package dev.latvian.mods.vidlib.feature.registry;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.latvian.mods.klib.data.RegisteredDataType;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.codec.DataArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public record RegistryOrDataArgumentType<T>(VLRegistry<T> registry, DataArgumentType<T> fallback) implements ArgumentType<T> {
	public static final SimpleCommandExceptionType VALUE_NOT_FOUND = new SimpleCommandExceptionType(Component.literal("Value not found"));

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		int i = reader.getCursor();

		try {
			return fallback.parse(reader);
		} catch (Exception ex) {
			reader.setCursor(i);
			var id = ID.parse(reader);
			var value = registry.get(id);

			if (value == null) {
				throw VALUE_NOT_FOUND.create();
			}

			return value;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		var input = builder.getRemaining().toLowerCase(Locale.ROOT);
		boolean col = input.indexOf(':') > -1;

		for (var id : registry.getMap().keySet()) {
			var ids = ID.idToString(id);

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

	public static class Info implements ArgumentTypeInfo<RegistryOrDataArgumentType<?>, RefHolderTemplate> {
		@Override
		public void serializeToNetwork(RefHolderTemplate template, FriendlyByteBuf buf) {
			buf.writeResourceLocation(template.commandDataType.registeredDataType.get().id());
		}

		@Override
		public RefHolderTemplate deserializeFromNetwork(FriendlyByteBuf buf) {
			return new RefHolderTemplate(this, CommandDataType.of(RegisteredDataType.BY_ID.get(buf.readResourceLocation()).type()));
		}

		@Override
		public void serializeToJson(RefHolderTemplate template, JsonObject json) {
			json.addProperty("codec", template.commandDataType.registeredDataType.get().id().toString());
		}

		@Override
		public RefHolderTemplate unpack(RegistryOrDataArgumentType arg) {
			return new RefHolderTemplate(this, arg.fallback.commandDataType());
		}
	}

	public record RefHolderTemplate(ArgumentTypeInfo<RegistryOrDataArgumentType<?>, ?> type, CommandDataType<?> commandDataType) implements ArgumentTypeInfo.Template<RegistryOrDataArgumentType<?>> {
		@Override
		public RegistryOrDataArgumentType<?> instantiate(CommandBuildContext ctx) {
			var ops = ctx.createSerializationContext(NbtOps.INSTANCE);
			var fallback = new DataArgumentType<>(ops, TagParser.create(ops), commandDataType);
			return new RegistryOrDataArgumentType<>((VLRegistry) commandDataType.registeredDataType.get().argumentType(), fallback);
		}
	}
}
