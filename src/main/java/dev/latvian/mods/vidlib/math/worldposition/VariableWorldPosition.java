package dev.latvian.mods.vidlib.math.worldposition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberContext;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record VariableWorldPosition(String name) implements WorldPosition {
	public static final SimpleRegistryType<VariableWorldPosition> TYPE = SimpleRegistryType.dynamic("variable", RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("name").forGetter(VariableWorldPosition::name)
	).apply(instance, VariableWorldPosition::new)), ByteBufCodecs.STRING_UTF8.map(VariableWorldPosition::new, VariableWorldPosition::name));

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Vec3 get(WorldNumberContext ctx) {
		return ctx.variables.positions().get(name).get(ctx);
	}
}