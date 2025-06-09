package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.visual.CubeTextures;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.LinkedHashMap;
import java.util.Map;

public record ZoneFluid(String id, FluidState fluidState, CubeTextures textures) {
	public static final ZoneFluid NONE = new ZoneFluid("none", Fluids.EMPTY.defaultFluidState(), CubeTextures.EMPTY);

	private static final Map<String, ZoneFluid> MAP = new LinkedHashMap<>();

	public static void register(ZoneFluid fluid) {
		MAP.put(fluid.id, fluid);
	}

	static {
		register(NONE);
		register(new ZoneFluid("water", Fluids.WATER.defaultFluidState(), CubeTextures.WATER));
		register(new ZoneFluid("lava", Fluids.LAVA.defaultFluidState(), CubeTextures.LAVA));
		register(new ZoneFluid("opaque_water", Fluids.WATER.defaultFluidState(), CubeTextures.OPAQUE_WATER));
		register(new ZoneFluid("pale_opaque_water", Fluids.WATER.defaultFluidState(), CubeTextures.PALE_OPAQUE_WATER));
	}

	public static final Codec<ZoneFluid> CODEC = KLibCodecs.map(MAP, Codec.STRING, ZoneFluid::id);
	public static final StreamCodec<ByteBuf, ZoneFluid> STREAM_CODEC = KLibStreamCodecs.map(MAP, ByteBufCodecs.STRING_UTF8, ZoneFluid::id).optional(NONE);

	public boolean isEmpty() {
		return fluidState.isEmpty();
	}
}
