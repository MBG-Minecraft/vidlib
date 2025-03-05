package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.util.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public record VariableWorldPosition(String name) implements WorldPosition {
	public static final SimpleRegistryType<VariableWorldPosition> TYPE = SimpleRegistryType.dynamic(Shimmer.id("variable"), RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableWorldPosition::name)
	).apply(instance, VariableWorldPosition::new)), ByteBufCodecs.STRING_UTF8.map(VariableWorldPosition::new, VariableWorldPosition::name));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public Vec3 get(WorldNumberContext ctx) {
		return Objects.requireNonNull(ctx.variables.positions().get(name).get(ctx));
	}
}