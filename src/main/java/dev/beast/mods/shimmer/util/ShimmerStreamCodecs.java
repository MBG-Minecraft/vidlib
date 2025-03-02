package dev.beast.mods.shimmer.util;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import dev.beast.mods.shimmer.Shimmer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public interface ShimmerStreamCodecs {
	StreamCodec<ByteBuf, UUID> UUID = new StreamCodec<>() {
		@Override
		public UUID decode(ByteBuf buf) {
			return new UUID(buf.readLong(), buf.readLong());
		}

		@Override
		public void encode(ByteBuf buf, UUID value) {
			buf.writeLong(value.getMostSignificantBits());
			buf.writeLong(value.getLeastSignificantBits());
		}
	};

	StreamCodec<RegistryFriendlyByteBuf, String> REGISTRY_STRING = new StreamCodec<>() {
		@Override
		public String decode(RegistryFriendlyByteBuf buf) {
			return buf.readUtf();
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, String value) {
			buf.writeUtf(value);
		}
	};

	StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> SHIMMER_ID = new StreamCodec<>() {
		@Override
		public ResourceLocation decode(RegistryFriendlyByteBuf buf) {
			var s = buf.readUtf();
			return s.indexOf(':') == -1 ? Shimmer.id(s) : ResourceLocation.parse(s);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ResourceLocation value) {
			buf.writeUtf(value.getNamespace().equals(Shimmer.ID) ? value.getPath() : value.toString());
		}
	};

	StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = ByteBufCodecs.TRUSTED_COMPOUND_TAG;

	StreamCodec<ByteBuf, Vec3> VEC_3 = new StreamCodec<>() {
		@Override
		public Vec3 decode(ByteBuf buf) {
			return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		}

		@Override
		public void encode(ByteBuf buf, Vec3 value) {
			buf.writeDouble(value.x());
			buf.writeDouble(value.y());
			buf.writeDouble(value.z());
		}
	};

	StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.VAR_INT.map(Block::stateById, Block::getId);

	static <B extends ByteBuf, V> StreamCodec<B, V> optional(StreamCodec<B, V> codec, @Nullable V defaultValue) {
		return new StreamCodec<>() {
			@Override
			public V decode(B buf) {
				return buf.readBoolean() ? codec.decode(buf) : defaultValue;
			}

			@Override
			public void encode(B buf, V value) {
				if (!Objects.equals(value, defaultValue)) {
					buf.writeBoolean(true);
					codec.encode(buf, value);
				} else {
					buf.writeBoolean(false);
				}
			}
		};
	}

	static <B extends ByteBuf, V> StreamCodec<B, V> nullable(StreamCodec<B, V> codec) {
		return optional(codec, null);
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> codec1,
		Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2,
		Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3,
		Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4,
		Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5,
		Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6,
		Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7,
		Function<C, T7> getter7,
		Function7<T1, T2, T3, T4, T5, T6, T7, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				var t5 = codec5.decode(buf);
				var t6 = codec6.decode(buf);
				var t7 = codec7.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
				codec6.encode(buf, getter6.apply(value));
				codec7.encode(buf, getter7.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> codec1,
		Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2,
		Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3,
		Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4,
		Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5,
		Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6,
		Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7,
		Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8,
		Function<C, T8> getter8,
		Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				var t5 = codec5.decode(buf);
				var t6 = codec6.decode(buf);
				var t7 = codec7.decode(buf);
				var t8 = codec8.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
				codec6.encode(buf, getter6.apply(value));
				codec7.encode(buf, getter7.apply(value));
				codec8.encode(buf, getter8.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> codec1,
		Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2,
		Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3,
		Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4,
		Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5,
		Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6,
		Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7,
		Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8,
		Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9,
		Function<C, T9> getter9,
		Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				var t5 = codec5.decode(buf);
				var t6 = codec6.decode(buf);
				var t7 = codec7.decode(buf);
				var t8 = codec8.decode(buf);
				var t9 = codec9.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
				codec6.encode(buf, getter6.apply(value));
				codec7.encode(buf, getter7.apply(value));
				codec8.encode(buf, getter8.apply(value));
				codec9.encode(buf, getter9.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> codec1,
		Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2,
		Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3,
		Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4,
		Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5,
		Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6,
		Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7,
		Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8,
		Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9,
		Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10,
		Function<C, T10> getter10,
		Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				var t5 = codec5.decode(buf);
				var t6 = codec6.decode(buf);
				var t7 = codec7.decode(buf);
				var t8 = codec8.decode(buf);
				var t9 = codec9.decode(buf);
				var t10 = codec10.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
				codec6.encode(buf, getter6.apply(value));
				codec7.encode(buf, getter7.apply(value));
				codec8.encode(buf, getter8.apply(value));
				codec9.encode(buf, getter9.apply(value));
				codec10.encode(buf, getter10.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> codec1,
		Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2,
		Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3,
		Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4,
		Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5,
		Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6,
		Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7,
		Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8,
		Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9,
		Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10,
		Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11,
		Function<C, T11> getter11,
		Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				var t5 = codec5.decode(buf);
				var t6 = codec6.decode(buf);
				var t7 = codec7.decode(buf);
				var t8 = codec8.decode(buf);
				var t9 = codec9.decode(buf);
				var t10 = codec10.decode(buf);
				var t11 = codec11.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
				codec6.encode(buf, getter6.apply(value));
				codec7.encode(buf, getter7.apply(value));
				codec8.encode(buf, getter8.apply(value));
				codec9.encode(buf, getter9.apply(value));
				codec10.encode(buf, getter10.apply(value));
				codec11.encode(buf, getter11.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(
		StreamCodec<? super B, T1> codec1,
		Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2,
		Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3,
		Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4,
		Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5,
		Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6,
		Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7,
		Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8,
		Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9,
		Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10,
		Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11,
		Function<C, T11> getter11,
		StreamCodec<? super B, T12> codec12,
		Function<C, T12> getter12,
		Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				var t5 = codec5.decode(buf);
				var t6 = codec6.decode(buf);
				var t7 = codec7.decode(buf);
				var t8 = codec8.decode(buf);
				var t9 = codec9.decode(buf);
				var t10 = codec10.decode(buf);
				var t11 = codec11.decode(buf);
				var t12 = codec12.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
				codec6.encode(buf, getter6.apply(value));
				codec7.encode(buf, getter7.apply(value));
				codec8.encode(buf, getter8.apply(value));
				codec9.encode(buf, getter9.apply(value));
				codec10.encode(buf, getter10.apply(value));
				codec11.encode(buf, getter11.apply(value));
				codec12.encode(buf, getter12.apply(value));
			}
		};
	}
}
