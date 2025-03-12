package dev.beast.mods.shimmer.feature.codec;

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function13;
import com.mojang.datafixers.util.Function14;
import com.mojang.datafixers.util.Function15;
import com.mojang.datafixers.util.Function16;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface CompositeStreamCodec {
	static <B, C, T1> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		Function<T1, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				return factory.apply(t1);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
			}
		};
	}

	static <B, C, T1, T2> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		BiFunction<T1, T2, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				return factory.apply(t1, t2);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		Function3<T1, T2, T3, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				return factory.apply(t1, t2, t3);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		Function4<T1, T2, T3, T4, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				return factory.apply(t1, t2, t3, t4);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		Function5<T1, T2, T3, T4, T5, C> factory
	) {
		return new StreamCodec<>() {
			@Override
			public C decode(B buf) {
				var t1 = codec1.decode(buf);
				var t2 = codec2.decode(buf);
				var t3 = codec3.decode(buf);
				var t4 = codec4.decode(buf);
				var t5 = codec5.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		Function6<T1, T2, T3, T4, T5, T6, C> factory
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
				return factory.apply(t1, t2, t3, t4, t5, t6);
			}

			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, getter1.apply(value));
				codec2.encode(buf, getter2.apply(value));
				codec3.encode(buf, getter3.apply(value));
				codec4.encode(buf, getter4.apply(value));
				codec5.encode(buf, getter5.apply(value));
				codec6.encode(buf, getter6.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
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

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
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

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
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

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
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

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
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

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
		StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
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

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
		StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
		StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
		Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, C> factory
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
				var t13 = codec13.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
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
				codec13.encode(buf, getter13.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
		StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
		StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
		StreamCodec<? super B, T14> codec14, Function<C, T14> getter14,
		Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, C> factory
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
				var t13 = codec13.decode(buf);
				var t14 = codec14.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
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
				codec13.encode(buf, getter13.apply(value));
				codec14.encode(buf, getter14.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
		StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
		StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
		StreamCodec<? super B, T14> codec14, Function<C, T14> getter14,
		StreamCodec<? super B, T15> codec15, Function<C, T15> getter15,
		Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, C> factory
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
				var t13 = codec13.decode(buf);
				var t14 = codec14.decode(buf);
				var t15 = codec15.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
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
				codec13.encode(buf, getter13.apply(value));
				codec14.encode(buf, getter14.apply(value));
				codec15.encode(buf, getter15.apply(value));
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> StreamCodec<B, C> of(
		StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
		StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
		StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
		StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
		StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
		StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
		StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
		StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
		StreamCodec<? super B, T9> codec9, Function<C, T9> getter9,
		StreamCodec<? super B, T10> codec10, Function<C, T10> getter10,
		StreamCodec<? super B, T11> codec11, Function<C, T11> getter11,
		StreamCodec<? super B, T12> codec12, Function<C, T12> getter12,
		StreamCodec<? super B, T13> codec13, Function<C, T13> getter13,
		StreamCodec<? super B, T14> codec14, Function<C, T14> getter14,
		StreamCodec<? super B, T15> codec15, Function<C, T15> getter15,
		StreamCodec<? super B, T16> codec16, Function<C, T16> getter16,
		Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, C> factory
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
				var t13 = codec13.decode(buf);
				var t14 = codec14.decode(buf);
				var t15 = codec15.decode(buf);
				var t16 = codec16.decode(buf);
				return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16);
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
				codec13.encode(buf, getter13.apply(value));
				codec14.encode(buf, getter14.apply(value));
				codec15.encode(buf, getter15.apply(value));
				codec16.encode(buf, getter16.apply(value));
			}
		};
	}
}
