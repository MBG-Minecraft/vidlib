package dev.latvian.mods.vidlib.feature.zone;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.visual.FluidTextures;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.LinkedHashMap;
import java.util.Map;

public record ZoneFluid(String id, FluidState fluidState, FluidTextures textures) {
	public static final ZoneFluid WATER = new ZoneFluid("water", Fluids.WATER.defaultFluidState(), FluidTextures.WATER);
	public static final ZoneFluid LAVA = new ZoneFluid("lava", Fluids.LAVA.defaultFluidState(), FluidTextures.LAVA);
	public static final ZoneFluid OPAQUE_WATER = new ZoneFluid("opaque_water", Fluids.WATER.defaultFluidState(), FluidTextures.OPAQUE_WATER);
	public static final ZoneFluid PALE_OPAQUE_WATER = new ZoneFluid("pale_opaque_water", Fluids.WATER.defaultFluidState(), FluidTextures.PALE_OPAQUE_WATER);

	public static final Map<String, ZoneFluid> MAP = new LinkedHashMap<>();

	public static void register(ZoneFluid fluid) {
		MAP.put(fluid.id, fluid);
	}

	static {
		register(WATER);
		register(LAVA);
		register(OPAQUE_WATER);
		register(PALE_OPAQUE_WATER);
	}

	public static final Codec<ZoneFluid> CODEC = KLibCodecs.map(MAP, Codec.STRING, ZoneFluid::id);
	public static final StreamCodec<ByteBuf, ZoneFluid> STREAM_CODEC = KLibStreamCodecs.map(MAP, ByteBufCodecs.STRING_UTF8, ZoneFluid::id);
}
