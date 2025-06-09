package dev.latvian.mods.vidlib.math.worldnumber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VariableWorldNumber(String name) implements WorldNumber {
	public static final SimpleRegistryType<VariableWorldNumber> TYPE = SimpleRegistryType.dynamic("variable", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableWorldNumber::name)
	).apply(instance, VariableWorldNumber::new)), ByteBufCodecs.STRING_UTF8.map(VariableWorldNumber::new, VariableWorldNumber::name));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(WorldNumberContext ctx) {
		var num = ctx.variables.numbers().get(name);
		return num == null ? null : num.get(ctx);
	}

	@Override
	@NotNull
	public String toString() {
		return name;
	}

	@Override
	public boolean isLiteral() {
		return true;
	}
}
