package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.npc.NPCParticleOptions;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface VidLibParticles {

	/**
	 * Copies the functionality of {@link net.minecraft.core.particles.SimpleParticleType}
	 * because NeoForge has an access transformer that opens the constructor.
	 */
	class SimpleParticleType extends ParticleType<SimpleParticleType> implements ParticleOptions {
		private final MapCodec<SimpleParticleType> codec = MapCodec.unit(this::getType);
		private final StreamCodec<RegistryFriendlyByteBuf, SimpleParticleType> streamCodec = StreamCodec.unit(this);

		public SimpleParticleType(boolean overrideLimiter) {
			super(overrideLimiter);
		}

		public SimpleParticleType getType() {
			return this;
		}

		public MapCodec<SimpleParticleType> codec() {
			return this.codec;
		}

		public StreamCodec<RegistryFriendlyByteBuf, SimpleParticleType> streamCodec() {
			return this.streamCodec;
		}
	}

	List<Pair<String, Supplier<? extends ParticleType<?>>>> PARTICLES = new ArrayList<>();

	static <T extends ParticleOptions> Supplier<ParticleType<T>> register(String name, Supplier<MapCodec<T>> codec, Supplier<StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec) {
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

	static Supplier<SimpleParticleType> register(String name) {
		Supplier<SimpleParticleType> supplier = Lazy.of(() -> new SimpleParticleType(true));
		PARTICLES.add(Pair.of(name, supplier));
		return supplier;
	}

	Supplier<ParticleType<ShapeParticleOptions>> SHAPE = register("shape", () -> ShapeParticleOptions.CODEC, () -> ShapeParticleOptions.STREAM_CODEC);
	Supplier<ParticleType<LineParticleOptions>> LINE = register("line", () -> LineParticleOptions.CODEC, () -> LineParticleOptions.STREAM_CODEC);
	Supplier<ParticleType<TextParticleOptions>> TEXT = register("text", () -> TextParticleOptions.CODEC, () -> TextParticleOptions.STREAM_CODEC);
	Supplier<ParticleType<ItemParticleOptions>> ITEM = register("item", () -> ItemParticleOptions.CODEC, () -> ItemParticleOptions.STREAM_CODEC);
	Supplier<ParticleType<NPCParticleOptions>> NPC = register("npc", () -> NPCParticleOptions.CODEC, () -> NPCParticleOptions.STREAM_CODEC);
	Supplier<ParticleType<LightningParticleOptions>> LIGHTNING = register("lightning", () -> LightningParticleOptions.CODEC, () -> LightningParticleOptions.STREAM_CODEC);
	Supplier<SimpleParticleType> BURN_SMOKE = register("burn_smoke");
	Supplier<SimpleParticleType> SPARK = register("spark");
	Supplier<ParticleType<WindParticleOptions>> WIND = register("wind", () -> WindParticleOptions.CODEC, () -> WindParticleOptions.STREAM_CODEC);
	Supplier<ParticleType<FireParticleOptions>> FIRE = register("fire", () -> FireParticleOptions.CODEC, () -> FireParticleOptions.STREAM_CODEC);
}
