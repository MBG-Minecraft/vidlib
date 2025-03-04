package dev.beast.mods.shimmer.feature.particle;

import com.mojang.serialization.MapCodec;
import dev.beast.mods.shimmer.Shimmer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface ShimmerParticles {
	DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, Shimmer.ID);

	static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> register(String name, boolean overrideLimit, Supplier<MapCodec<T>> codec, Supplier<StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec) {
		return REGISTRY.register(name, () -> new ParticleType<>(overrideLimit) {
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

	DeferredHolder<ParticleType<?>, ParticleType<CubeParticleOptions>> CUBE = register("cube", true, () -> CubeParticleOptions.CODEC, () -> CubeParticleOptions.STREAM_CODEC);
}
