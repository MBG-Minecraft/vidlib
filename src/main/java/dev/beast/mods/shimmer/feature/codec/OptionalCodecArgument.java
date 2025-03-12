package dev.beast.mods.shimmer.feature.codec;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public record OptionalCodecArgument<T>(DynamicOps<Tag> ops, KnownCodec<T> knownCodec) implements ArgumentType<Optional<T>> {
	public static final DynamicCommandExceptionType ERROR_PARSING = new DynamicCommandExceptionType(arg -> Component.literal(String.valueOf(arg)));

	@Override
	public Optional<T> parse(StringReader reader) throws CommandSyntaxException {
		var tag = new TagParser(reader).readValue();

		if (tag instanceof CompoundTag t && t.isEmpty()) {
			return Optional.empty();
		}

		var decoded = knownCodec.codec().decode(ops, tag);

		if (decoded.isError()) {
			throw ERROR_PARSING.create(decoded.error().get().message());
		}

		return Optional.of(decoded.getOrThrow().getFirst());
	}

	public static class Info implements ArgumentTypeInfo<OptionalCodecArgument<?>, CodecTemplate> {
		@Override
		public void serializeToNetwork(CodecTemplate template, FriendlyByteBuf buf) {
			buf.writeResourceLocation(template.knownCodec.id());
		}

		@Override
		public CodecTemplate deserializeFromNetwork(FriendlyByteBuf buf) {
			return new CodecTemplate(this, KnownCodec.MAP.get(buf.readResourceLocation()));
		}

		@Override
		public void serializeToJson(CodecTemplate template, JsonObject json) {
			json.addProperty("codec", template.knownCodec.id().toString());
		}

		@Override
		public CodecTemplate unpack(OptionalCodecArgument arg) {
			return new CodecTemplate(this, arg.knownCodec);
		}
	}

	public record CodecTemplate(ArgumentTypeInfo<OptionalCodecArgument<?>, ?> type, KnownCodec<?> knownCodec) implements ArgumentTypeInfo.Template<OptionalCodecArgument<?>> {
		@Override
		public OptionalCodecArgument<?> instantiate(CommandBuildContext ctx) {
			return knownCodec.optionalArgument(ctx);
		}
	}
}
