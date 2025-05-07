package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.codec.VLCodecs;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import dev.latvian.mods.vidlib.util.CubeTextures;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public record ZoneFluid(String id, FluidState fluidState, CubeTextures textures) {
	public static final ZoneFluid EMPTY = new ZoneFluid("empty", Fluids.EMPTY.defaultFluidState(), CubeTextures.EMPTY);

	private static final Map<String, ZoneFluid> MAP = new LinkedHashMap<>();

	public static void register(ZoneFluid fluid) {
		MAP.put(fluid.id, fluid);
	}

	static {
		register(EMPTY);
		register(new ZoneFluid("water", Fluids.WATER.defaultFluidState(), CubeTextures.WATER));
		register(new ZoneFluid("lava", Fluids.LAVA.defaultFluidState(), CubeTextures.LAVA));
		register(new ZoneFluid("opaque_water", Fluids.WATER.defaultFluidState(), CubeTextures.OPAQUE_WATER));
		register(new ZoneFluid("pale_opaque_water", Fluids.WATER.defaultFluidState(), CubeTextures.PALE_OPAQUE_WATER));
	}

	public static final Codec<ZoneFluid> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.fieldOf("id").forGetter(ZoneFluid::id),
		FluidState.CODEC.fieldOf("fluid").forGetter(ZoneFluid::fluidState),
		CubeTextures.CODEC.fieldOf("textures").forGetter(ZoneFluid::textures)
	).apply(instance, ZoneFluid::new));

	public static final Codec<ZoneFluid> REGISTRY_CODEC = VLCodecs.map(MAP, Codec.STRING, ZoneFluid::id);
	public static final Codec<ZoneFluid> CODEC = Codec.either(REGISTRY_CODEC, DIRECT_CODEC).xmap(either -> either.map(Function.identity(), Function.identity()), fluid -> MAP.containsValue(fluid) ? Either.left(fluid) : Either.right(fluid));
	public static final StreamCodec<ByteBuf, ZoneFluid> STREAM_CODEC = VLStreamCodecs.map(MAP, ByteBufCodecs.STRING_UTF8, ZoneFluid::id).optional(EMPTY);

	public boolean isEmpty() {
		return fluidState.isEmpty();
	}
}
