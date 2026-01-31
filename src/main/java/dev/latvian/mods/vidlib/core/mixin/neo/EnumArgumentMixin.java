package dev.latvian.mods.vidlib.core.mixin.neo;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.latvian.mods.vidlib.util.EnumCommandName;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.server.command.EnumArgument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(EnumArgument.class)
public class EnumArgumentMixin<T extends Enum<T>> {
	@Shadow
	@Final
	private Class<T> enumClass;

	@Unique
	private Map<T, String> vl$enumValues;

	@Redirect(method = "parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Enum;", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;readUnquotedString()Ljava/lang/String;"))
	private String vl$readString(StringReader instance) throws CommandSyntaxException {
		return instance.readString();
	}

	@Unique
	private Map<T, String> vl$enumValues() {
		if (vl$enumValues == null) {
			vl$enumValues = new EnumMap<>(enumClass);

			for (var value : enumClass.getEnumConstants()) {
				vl$enumValues.put(value, value instanceof EnumCommandName e ? e.getCommandName() : value instanceof StringRepresentable v ? v.getSerializedName() : value.name().toLowerCase(Locale.ROOT));
			}
		}

		return vl$enumValues;
	}

	@Redirect(method = "parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Enum;", at = @At(value = "INVOKE", target = "Ljava/lang/Enum;valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;"))
	private T vl$betterName(Class<T> enumClass, String name) {
		var map = vl$enumValues();

		for (var entry : map.entrySet()) {
			if (entry.getValue().equalsIgnoreCase(name)) {
				return entry.getKey();
			}
		}

		throw new IllegalArgumentException();
	}

	@Redirect(method = {"getExamples", "listSuggestions", "parse(Lcom/mojang/brigadier/StringReader;)Ljava/lang/Enum;"}, at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
	private Stream<String> vl$betterName(Stream<T> instance, Function<? super T, ? extends String> function) {
		var map = vl$enumValues();
		return instance.map(t -> StringArgumentType.escapeIfRequired(Objects.requireNonNull(map.get(t))));
	}
}
