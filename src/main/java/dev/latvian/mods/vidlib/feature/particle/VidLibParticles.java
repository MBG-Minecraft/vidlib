package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.actor.ActorParticleOptions;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public interface VidLibParticles {
	List<Pair<Identifier, Supplier<? extends ParticleType<?>>>> PARTICLES = new LinkedList<>();

	static <T extends ParticleOptions> Supplier<ParticleType<T>> register(Identifier name, Supplier<MapCodec<T>> codec, Supplier<StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec) {
		Supplier<ParticleType<T>> supplier = Lazy.of(() -> new ParticleType<>(true) {
			@Override
			public MapCodec<T> codec() {
				return codec.get();
			}

			@Override
			public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
				return streamCodec.get();
			}
		});

		PARTICLES.add(Pair.of(name, supplier));
		return supplier;
	}

	static Supplier<SimpleParticleType> register(Identifier name) {
		Supplier<SimpleParticleType> supplier = Lazy.of(() -> new SimpleParticleType(true));
		PARTICLES.add(Pair.of(name, supplier));
		return supplier;
	}

	static void init() {
	}

	Supplier<ParticleType<ShapeParticleOptions>> SHAPE = register(ID.vidlib("shape"), () -> ShapeParticleOptions.MAP_CODEC, () -> ShapeParticleOptions.STREAM_CODEC);
	// Supplier<ParticleType<LineParticleOptions>> LINE = register(ID.vidlib("line"), () -> LineParticleOptions.MAP_CODEC, () -> LineParticleOptions.STREAM_CODEC);
	// Supplier<ParticleType<TextParticleOptions>> TEXT = register(ID.vidlib("text"), () -> TextParticleOptions.MAP_CODEC, () -> TextParticleOptions.STREAM_CODEC);
	// Supplier<ParticleType<ItemParticleOptions>> ITEM = register(ID.vidlib("item"), () -> ItemParticleOptions.MAP_CODEC, () -> ItemParticleOptions.STREAM_CODEC);
	Supplier<ParticleType<ActorParticleOptions>> ACTOR = register(ID.vidlib("actor"), () -> ActorParticleOptions.MAP_CODEC, () -> ActorParticleOptions.STREAM_CODEC);
	// Supplier<ParticleType<LightningParticleOptions>> LIGHTNING = register(ID.vidlib("lightning"), () -> LightningParticleOptions.MAP_CODEC, () -> LightningParticleOptions.STREAM_CODEC);
	// Supplier<SimpleParticleType> BURN_SMOKE = register(ID.vidlib("burn_smoke"));
	// Supplier<SimpleParticleType> SPARK = register(ID.vidlib("spark"));
	// Supplier<ParticleType<WindParticleOptions>> WIND = register(ID.vidlib("wind"), () -> WindParticleOptions.MAP_CODEC, () -> WindParticleOptions.STREAM_CODEC);
	// Supplier<ParticleType<FireParticleOptions>> FIRE = register(ID.vidlib("fire"), () -> FireParticleOptions.MAP_CODEC, () -> FireParticleOptions.STREAM_CODEC);
}
