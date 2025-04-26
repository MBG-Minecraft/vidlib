package dev.beast.mods.shimmer.core.mixin.neo;

import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.server.command.EnumArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(EnumArgument.class)
public class EnumArgumentMixin<T extends Enum<T>> {
	@Redirect(method = "parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Enum;", at = @At(value = "INVOKE", target = "Ljava/lang/Enum;valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;"))
	private T vl$betterName(Class<T> enumClass, String name) {
		for (var value : enumClass.getEnumConstants()) {
			if (value instanceof StringRepresentable v ? v.getSerializedName().equalsIgnoreCase(name) : value.name().equalsIgnoreCase(name)) {
				return value;
			}
		}

		throw new IllegalArgumentException();
	}

	@Redirect(method = {"getExamples", "listSuggestions", "parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Enum;"}, at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
	private Stream<String> vl$betterName(Stream<T> instance, Function<? super T, ? extends String> function) {
		return instance.map(t -> t instanceof StringRepresentable v ? v.getSerializedName() : t.name().toLowerCase(Locale.ROOT));
	}
}
