package dev.latvian.mods.vidlib.feature.codec;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.klib.util.ID;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public interface VLStreamCodecs {
	static Either<Object, byte[]> decode(RegistryFriendlyByteBuf buf) {
		int data = buf.readVarInt();

		return switch (data) {
			case 0 -> Either.left(null);
			case 1 -> Either.right(buf.readByteArray(Integer.MAX_VALUE));
			case 2 -> Either.left(Boolean.FALSE);
			case 3 -> Either.left(Boolean.TRUE);
			case 4 -> Either.left(buf.readVarInt());
			case 5 -> Either.left(buf.readVarLong());
			case 6 -> Either.left(buf.readByte());
			case 7 -> Either.left(buf.readShort());
			case 8 -> Either.left(buf.readInt());
			case 9 -> Either.left(buf.readLong());
			case 10 -> Either.left(buf.readFloat());
			case 11 -> Either.left(buf.readDouble());
			case 12 -> Either.left(buf.readUtf());
			case 13 -> Either.left(ID.STREAM_CODEC.decode(buf));
			case 14 -> Either.left(ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf));
			case 15 -> Either.left(buf.readBlockPos());
			default -> throw new IllegalArgumentException("Unknown data type: " + data);
		};
	}

	static Object decodeValue(RegistryFriendlyByteBuf buf, DataType<?> type, byte[] bytes) {
		var buf1 = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), buf.registryAccess(), buf.getConnectionType());

		try {
			return type.streamCodec().decode(buf1);
		} finally {
			buf1.release();
		}
	}

	static void encode(RegistryFriendlyByteBuf buf, DataType<?> type, Object value) {
		if (value == null) {
			buf.writeVarInt(0);
		} else if (value instanceof Boolean v) {
			buf.writeVarInt(v ? 3 : 2);
		} else if (type == DataTypes.VAR_INT) {
			buf.writeVarInt(4);
			buf.writeVarInt((Integer) value);
		} else if (type == DataTypes.VAR_LONG) {
			buf.writeVarInt(5);
			buf.writeVarLong((Long) value);
		} else if (value instanceof Byte v) {
			buf.writeVarInt(6);
			buf.writeByte(v);
		} else if (value instanceof Short v) {
			buf.writeVarInt(7);
			buf.writeShort(v);
		} else if (value instanceof Integer v) {
			buf.writeVarInt(8);
			buf.writeInt(v);
		} else if (value instanceof Long v) {
			buf.writeVarInt(9);
			buf.writeLong(v);
		} else if (value instanceof Float v) {
			buf.writeVarInt(10);
			buf.writeFloat(v);
		} else if (value instanceof Double v) {
			buf.writeVarInt(11);
			buf.writeDouble(v);
		} else if (value instanceof String v) {
			buf.writeVarInt(12);
			buf.writeUtf(v);
		} else if (value instanceof ResourceLocation v) {
			buf.writeVarInt(13);
			ID.STREAM_CODEC.encode(buf, v);
		} else if (value instanceof Component v) {
			buf.writeVarInt(14);
			ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, v);
		} else if (value instanceof BlockPos v) {
			buf.writeVarInt(15);
			buf.writeBlockPos(v);
		} else {
			buf.writeVarInt(1);
			var buf1 = new RegistryFriendlyByteBuf(Unpooled.buffer(), buf.registryAccess(), buf.getConnectionType());

			try {
				type.streamCodec().encode(buf1, Cast.to(value));
				var bytes = new byte[buf1.readableBytes()];
				buf1.getBytes(buf1.readerIndex(), bytes);
				buf.writeByteArray(bytes);
			} finally {
				buf1.release();
			}
		}
	}
}
