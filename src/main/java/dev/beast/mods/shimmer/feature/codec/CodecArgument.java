package dev.beast.mods.shimmer.feature.codec;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record CodecArgument<T>(DynamicOps<Tag> ops, KnownCodec<T> knownCodec) implements ArgumentType<T> {
	public static final DynamicCommandExceptionType ERROR_PARSING = new DynamicCommandExceptionType(arg -> Component.literal(String.valueOf(arg)));

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		var tag = new TagParser(reader).readValue();
		var decoded = knownCodec.codec().parse(ops, tag);

		if (decoded.isError()) {
			throw ERROR_PARSING.create(decoded.error().get().message());
		}

		return decoded.getOrThrow();
	}

	public static class Info implements ArgumentTypeInfo<CodecArgument<?>, CodecTemplate> {
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
		public CodecTemplate unpack(CodecArgument arg) {
			return new CodecTemplate(this, arg.knownCodec);
		}
	}

	public record CodecTemplate(ArgumentTypeInfo<CodecArgument<?>, ?> type, KnownCodec<?> knownCodec) implements ArgumentTypeInfo.Template<CodecArgument<?>> {
		@Override
		public CodecArgument<?> instantiate(CommandBuildContext ctx) {
			return (CodecArgument<?>) knownCodec.argument(ctx);
		}
	}
}
