package dev.beast.mods.shimmer.util;

import dev.beast.mods.shimmer.Shimmer;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

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

	StreamCodec<ByteBuf, ResourceLocation> SHIMMER_ID = new StreamCodec<>() {
		@Override
		public ResourceLocation decode(ByteBuf buf) {
			var s = Utf8String.read(buf, Short.MAX_VALUE);
			return s.indexOf(':') == -1 ? Shimmer.id(s) : ResourceLocation.parse(s);
		}

		@Override
		public void encode(ByteBuf buf, ResourceLocation value) {
			Utf8String.write(buf, value.getNamespace().equals(Shimmer.ID) ? value.getPath() : value.toString(), Short.MAX_VALUE);
		}
	};

	StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> REGISTRY_SHIMMER_ID = new StreamCodec<>() {
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

	StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> REGISTRY_ID = new StreamCodec<>() {
		@Override
		public ResourceLocation decode(RegistryFriendlyByteBuf buf) {
			var s = buf.readUtf();
			return s.indexOf(':') == -1 ? ResourceLocation.withDefaultNamespace(s) : ResourceLocation.parse(s);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ResourceLocation value) {
			buf.writeUtf(value.getNamespace().equals("minecraft") ? value.getPath() : value.toString());
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

	StreamCodec<ByteBuf, ResourceKey<Level>> DIMENSION = ResourceLocation.STREAM_CODEC.map(id -> ResourceKey.create(Registries.DIMENSION, id), ResourceKey::location);

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

	static <B extends ByteBuf, K, V> StreamCodec<B, V> map(Supplier<Map<K, V>> mapGetter, StreamCodec<B, K> keyCodec, Function<V, K> keyGetter) {
		return keyCodec.map(key -> mapGetter.get().get(key), keyGetter);
	}

	static <B extends ByteBuf, K, V> StreamCodec<B, V> map(Map<K, V> map, StreamCodec<B, K> keyCodec, Function<V, K> keyGetter) {
		Objects.requireNonNull(map, "Map is null");
		return keyCodec.map(map::get, keyGetter);
	}
}
