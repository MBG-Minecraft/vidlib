package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.MapCodec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface ShimmerParticles {
	@AutoRegister
	DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, Shimmer.ID);

	static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> register(String name, Supplier<MapCodec<T>> codec, Supplier<StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec) {
		return REGISTRY.register(name, () -> new ParticleType<>(true) {
			@Override
			public MapCodec<T> codec() {
				return codec.get();
			}

			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
				return streamCodec.get();
			}
		});
	}

	static DeferredHolder<ParticleType<?>, SimpleParticleType> register(String name) {
		return REGISTRY.register(name, () -> new SimpleParticleType(true));
	}

	DeferredHolder<ParticleType<?>, ParticleType<CubeParticleOptions>> CUBE = register("cube", () -> CubeParticleOptions.CODEC, () -> CubeParticleOptions.STREAM_CODEC);
	DeferredHolder<ParticleType<?>, ParticleType<LineParticleOptions>> LINE = register("line", () -> LineParticleOptions.CODEC, () -> LineParticleOptions.STREAM_CODEC);
	DeferredHolder<ParticleType<?>, SimpleParticleType> BURN_SMOKE = register("burn_smoke");
	DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK = register("spark");
	DeferredHolder<ParticleType<?>, ParticleType<WindParticleOptions>> WIND = register("wind", () -> WindParticleOptions.CODEC, () -> WindParticleOptions.STREAM_CODEC);
}
