package dev.latvian.mods.vidlib.math.worldvector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VariableWorldVector(String name) implements WorldVector {
	public static final SimpleRegistryType<VariableWorldVector> TYPE = SimpleRegistryType.dynamic("variable", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableWorldVector::name)
	).apply(instance, VariableWorldVector::new)), ByteBufCodecs.STRING_UTF8.map(VariableWorldVector::new, VariableWorldVector::name));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		var pos = ctx.variables.vectors().get(name);

		if (pos == null) {
			var num = ctx.variables.numbers().get(name);

			if (num != null) {
				var d = num.get(ctx);

				if (d == null) {
					return null;
				}

				return KMath.vec3(d, d, d);
			}
		}

		return pos == null ? null : pos.get(ctx);
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