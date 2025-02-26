package dev.beast.mods.shimmer.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.UUID;

public interface ShimmerStreamCodecs {
	StreamCodec<ByteBuf, UUID> UUID = new StreamCodec<>() {
		@Override
		public UUID decode(ByteBuf buf) {
			return new UUID(buf.readLong(), buf.readLong());
		}

		@Override
		public void encode(ByteBuf buf, UUID uuid) {
			buf.writeLong(uuid.getMostSignificantBits());
			buf.writeLong(uuid.getLeastSignificantBits());
		}
	};

	StreamCodec<FriendlyByteBuf, CompoundTag> COMPOUND_TAG = new StreamCodec<>() {
		@Override
		public CompoundTag decode(FriendlyByteBuf buf) {
			return (CompoundTag) buf.readNbt(NbtAccounter.unlimitedHeap());
		}

		@Override
		public void encode(FriendlyByteBuf buf, CompoundTag tag) {
			buf.writeNbt(tag);
		}
	};

	static <B extends ByteBuf, V> StreamCodec<B, V> optional(StreamCodec<B, V> codec, V defaultValue) {
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
}
