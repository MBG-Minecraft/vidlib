package dev.latvian.mods.vidlib.feature.codec;

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

public record RegisteredDataTypeArgument<T>(DynamicOps<Tag> ops, TagParser<Tag> parser, RegisteredDataType<T> dataType) implements ArgumentType<T> {
	public static final DynamicCommandExceptionType ERROR_PARSING = new DynamicCommandExceptionType(arg -> Component.literal(String.valueOf(arg)));

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		var tag = parser.parseAsArgument(reader);
		var decoded = dataType.type().codec().parse(ops, tag);

		if (decoded.isError()) {
			throw ERROR_PARSING.create(decoded.error().get().message());
		}

		return decoded.getOrThrow();
	}

	public static class Info implements ArgumentTypeInfo<RegisteredDataTypeArgument<?>, CodecTemplate> {
		@Override
		public void serializeToNetwork(CodecTemplate template, FriendlyByteBuf buf) {
			buf.writeResourceLocation(template.dataType.id());
		}

		@Override
		public CodecTemplate deserializeFromNetwork(FriendlyByteBuf buf) {
			return new CodecTemplate(this, RegisteredDataType.REGISTRY.get(buf.readResourceLocation()));
		}

		@Override
		public void serializeToJson(CodecTemplate template, JsonObject json) {
			json.addProperty("codec", template.dataType.id().toString());
		}

		@Override
		public CodecTemplate unpack(RegisteredDataTypeArgument arg) {
			return new CodecTemplate(this, arg.dataType);
		}
	}

	public record CodecTemplate(ArgumentTypeInfo<RegisteredDataTypeArgument<?>, ?> type, RegisteredDataType<?> dataType) implements ArgumentTypeInfo.Template<RegisteredDataTypeArgument<?>> {
		@Override
		public RegisteredDataTypeArgument<?> instantiate(CommandBuildContext ctx) {
			return (RegisteredDataTypeArgument<?>) dataType.argument(ctx);
		}
	}
}
