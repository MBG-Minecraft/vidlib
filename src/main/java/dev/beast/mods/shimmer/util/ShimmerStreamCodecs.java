package dev.beast.mods.shimmer.util;

import dev.beast.mods.shimmer.Shimmer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

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
