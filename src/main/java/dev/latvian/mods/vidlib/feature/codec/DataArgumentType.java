package dev.latvian.mods.vidlib.feature.codec;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.data.RegisteredDataType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record DataArgumentType<T>(DynamicOps<Tag> ops, TagParser<Tag> parser, CommandDataType<T> commandDataType) implements ArgumentType<T> {
	public static final DynamicCommandExceptionType ERROR_PARSING = new DynamicCommandExceptionType(arg -> Component.literal(String.valueOf(arg)));

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		var tag = parser.parseAsArgument(reader);
		var decoded = commandDataType.dataType.codec().parse(ops, tag);

		if (decoded.isError()) {
			throw ERROR_PARSING.create(decoded.error().get().message());
		}

		return decoded.getOrThrow();
	}

	public static class Info implements ArgumentTypeInfo<DataArgumentType<?>, CodecTemplate> {
		@Override
		public void serializeToNetwork(CodecTemplate template, FriendlyByteBuf buf) {
			buf.writeResourceLocation(template.commandDataType.registeredDataType.get().id());
		}

		@Override
		public CodecTemplate deserializeFromNetwork(FriendlyByteBuf buf) {
			return new CodecTemplate(this, CommandDataType.of(RegisteredDataType.BY_ID.get(buf.readResourceLocation()).type()));
		}

		@Override
		public void serializeToJson(CodecTemplate template, JsonObject json) {
			json.addProperty("codec", template.commandDataType.registeredDataType.get().id().toString());
		}

		@Override
		public CodecTemplate unpack(DataArgumentType arg) {
			return new CodecTemplate(this, arg.commandDataType);
		}
	}

	public record CodecTemplate(ArgumentTypeInfo<DataArgumentType<?>, ?> type, CommandDataType<?> commandDataType) implements ArgumentTypeInfo.Template<DataArgumentType<?>> {
		@Override
		public DataArgumentType<?> instantiate(CommandBuildContext ctx) {
			var ops = ctx.createSerializationContext(NbtOps.INSTANCE);
			return new DataArgumentType<>(ops, TagParser.create(ops), commandDataType);
		}
	}
}
