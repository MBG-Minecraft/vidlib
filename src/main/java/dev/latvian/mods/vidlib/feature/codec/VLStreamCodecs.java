package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import dev.latvian.mods.vidlib.util.Empty;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortLists;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public interface VLStreamCodecs {
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

	StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = new StreamCodec<>() {
		@Override
		public CompoundTag decode(ByteBuf buf) {
			var size = VarInt.read(buf);

			if (size == 0) {
				return Empty.COMPOUND_TAG;
			}

			var tag = new CompoundTag();

			for (int i = 0; i < size; i++) {
				tag.put(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.TAG.decode(buf));
			}

			return tag;
		}

		@Override
		public void encode(ByteBuf buf, CompoundTag value) {
			VarInt.write(buf, value.size());

			if (!value.isEmpty()) {
				for (var key : value.keySet()) {
					ByteBufCodecs.STRING_UTF8.encode(buf, key);
					ByteBufCodecs.TAG.encode(buf, value.get(key));
				}
			}
		}
	};

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
	StreamCodec<ByteBuf, FluidState> FLUID_STATE = ByteBufCodecs.VAR_INT.map(Fluid.FLUID_STATE_REGISTRY::byId, Fluid.FLUID_STATE_REGISTRY::getId);

	StreamCodec<ByteBuf, ResourceKey<Level>> DIMENSION = resourceKey(Registries.DIMENSION);

	StreamCodec<ByteBuf, IntList> VAR_INT_LIST = new StreamCodec<>() {
		@Override
		public IntList decode(ByteBuf buf) {
			int size = VarInt.read(buf);

			if (size == 0) {
				return IntLists.emptyList();
			} else if (size == 1) {
				return IntLists.singleton(VarInt.read(buf));
			} else {
				var list = new IntArrayList(size);

				for (int i = 0; i < size; i++) {
					list.add(VarInt.read(buf));
				}

				return list;
			}
		}

		@Override
		public void encode(ByteBuf buf, IntList value) {
			VarInt.write(buf, value.size());

			for (int i = 0; i < value.size(); i++) {
				VarInt.write(buf, value.getInt(i));
			}
		}
	};

	StreamCodec<ByteBuf, LongList> LONG_LIST = new StreamCodec<>() {
		@Override
		public LongList decode(ByteBuf buf) {
			int size = VarInt.read(buf);

			if (size == 0) {
				return LongLists.emptyList();
			} else if (size == 1) {
				return LongLists.singleton(buf.readLong());
			} else {
				var list = new LongArrayList(size);

				for (int i = 0; i < size; i++) {
					list.add(buf.readLong());
				}

				return list;
			}
		}

		@Override
		public void encode(ByteBuf buf, LongList value) {
			VarInt.write(buf, value.size());

			for (int i = 0; i < value.size(); i++) {
				buf.writeLong(value.getLong(i));
			}
		}
	};

	StreamCodec<ByteBuf, Double> DOUBLE_AS_FLOAT = new StreamCodec<>() {
		@Override
		public Double decode(ByteBuf buf) {
			return (double) buf.readFloat();
		}

		@Override
		public void encode(ByteBuf buf, Double value) {
			buf.writeFloat(value.floatValue());
		}
	};

	StreamCodec<ByteBuf, AABB> AABB = StreamCodec.composite(
		ByteBufCodecs.DOUBLE, b -> b.minX,
		ByteBufCodecs.DOUBLE, b -> b.minY,
		ByteBufCodecs.DOUBLE, b -> b.minZ,
		ByteBufCodecs.DOUBLE, b -> b.maxX,
		ByteBufCodecs.DOUBLE, b -> b.maxY,
		ByteBufCodecs.DOUBLE, b -> b.maxZ,
		AABB::new
	);

	StreamCodec<ByteBuf, Unit> UNIT = StreamCodec.unit(Unit.INSTANCE);
	StreamCodec<ByteBuf, SectionPos> SECTION_POS = ByteBufCodecs.LONG.map(SectionPos::of, SectionPos::asLong);

	StreamCodec<ByteBuf, ShortList> SHORT_LIST = new StreamCodec<>() {
		@Override
		public ShortList decode(ByteBuf buf) {
			int size = VarInt.read(buf);

			if (size == 0) {
				return ShortLists.emptyList();
			} else if (size == 1) {
				return ShortLists.singleton((short) VarInt.read(buf));
			} else {
				var list = new ShortArrayList(size);

				for (int i = 0; i < size; i++) {
					list.add((short) VarInt.read(buf));
				}

				return list;
			}
		}

		@Override
		public void encode(ByteBuf buf, ShortList value) {
			VarInt.write(buf, value.size());

			for (int i = 0; i < value.size(); i++) {
				VarInt.write(buf, value.getShort(i));
			}
		}
	};

	static <T> StreamCodec<ByteBuf, ResourceKey<T>> resourceKey(ResourceKey<? extends Registry<T>> registry) {
		return ResourceLocation.STREAM_CODEC.map(id -> ResourceKey.create(registry, id), ResourceKey::location);
	}

	static <T> StreamCodec<ByteBuf, TagKey<T>> tagKey(ResourceKey<? extends Registry<T>> registry) {
		return ResourceLocation.STREAM_CODEC.map(id -> TagKey.create(registry, id), TagKey::location);
	}

	static <B extends ByteBuf, K, V> StreamCodec<B, V> map(Supplier<Map<K, V>> mapGetter, StreamCodec<B, K> keyCodec, Function<V, K> keyGetter) {
		return keyCodec.map(key -> mapGetter.get().get(key), keyGetter);
	}

	static <B extends ByteBuf, K, V> StreamCodec<B, V> map(Map<K, V> map, StreamCodec<B, K> keyCodec, Function<V, K> keyGetter) {
		Objects.requireNonNull(map, "Map is null");
		return keyCodec.map(map::get, keyGetter);
	}

	static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumValue(Class<E> enumClass) {
		return enumValue(enumClass.getEnumConstants());
	}

	static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumValue(E[] values) {
		return ByteBufCodecs.idMapper(i -> values[i], Enum::ordinal);
	}

	static <T> StreamCodec<ByteBuf, T> registry(Registry<T> registry) {
		return ByteBufCodecs.VAR_INT.map(registry::byIdOrThrow, registry::getId);
	}

	static <B extends ByteBuf, L, R> StreamCodec<B, Pair<L, R>> pair(StreamCodec<? super B, L> left, StreamCodec<? super B, R> right) {
		return StreamCodec.composite(left, Pair::getFirst, right, Pair::getSecond, Pair::of);
	}
}
