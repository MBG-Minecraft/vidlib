package dev.beast.mods.shimmer.util.registry;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record RefHolderArgument<T>(ShimmerRegistry<T> idHolder, KnownCodec<T> knownCodec) implements ArgumentType<T> {
	public static final SimpleCommandExceptionType VALUE_NOT_FOUND = new SimpleCommandExceptionType(Component.literal("Value not found"));

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		var id = idHolder.preferInternal ? ShimmerResourceLocationArgument.parse0(reader) : VideoResourceLocationArgument.parse0(reader);
		var value = idHolder.get(id);

		if (value == null) {
			throw VALUE_NOT_FOUND.create();
		}

		return value;
	}

	public static class Info implements ArgumentTypeInfo<RefHolderArgument<?>, RefHolderTemplate> {
		@Override
		public void serializeToNetwork(RefHolderTemplate template, FriendlyByteBuf buf) {
			buf.writeResourceLocation(template.knownCodec.id());
		}

		@Override
		public RefHolderTemplate deserializeFromNetwork(FriendlyByteBuf buf) {
			return new RefHolderTemplate(this, KnownCodec.MAP.get(buf.readResourceLocation()));
		}

		@Override
		public void serializeToJson(RefHolderTemplate template, JsonObject json) {
			json.addProperty("codec", template.knownCodec.id().toString());
		}

		@Override
		public RefHolderTemplate unpack(RefHolderArgument arg) {
			return new RefHolderTemplate(this, arg.knownCodec);
		}
	}

	public record RefHolderTemplate(ArgumentTypeInfo<RefHolderArgument<?>, ?> type, KnownCodec<?> knownCodec) implements ArgumentTypeInfo.Template<RefHolderArgument<?>> {
		@Override
		public RefHolderArgument<?> instantiate(CommandBuildContext ctx) {
			return (RefHolderArgument<?>) knownCodec.argument(ctx);
		}
	}
}
