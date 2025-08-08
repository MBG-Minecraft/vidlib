package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public record PropPacketType<P extends Prop, T>(Handler<P, T> handler, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) implements PropTypeInfo {
	public static final Object UNIT = new Object();
	private static final StreamCodec<ByteBuf, Object> UNIT_STREAM_CODEC = StreamCodec.unit(UNIT);

	public interface Handler<P extends Prop, T> {
		void handle(P prop, Context ctx, T payload);
	}

	public static <P extends Prop, T> PropPacketType<P, T> of(Handler<P, T> handler, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return new PropPacketType<>(handler, streamCodec);
	}

	public static <P extends Prop> PropPacketType<P, Object> unit(BiConsumer<P, Context> handler) {
		return of((prop, ctx, payload) -> handler.accept(prop, ctx), UNIT_STREAM_CODEC);
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Nullable
	public SimplePacketPayload createPayload(Prop prop, T payload) {
		int index = prop.type.getPacketIndex(this);

		if (index == -1) {
			throw new NullPointerException("Packet not registered in PropType");
		}

		RegistryFriendlyByteBuf buf = null;

		try {
			buf = PlatformHelper.CURRENT.createBuffer(Unpooled.buffer(), prop.level.registryAccess());
			streamCodec.encode(buf, payload);
			var bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			return prop.level.isClientSide() ? new PropC2SPayload(prop.id, index, bytes) : new PropS2CPayload(prop.id, index, bytes);
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to create a Prop packet", ex);
			return null;
		} finally {
			if (buf != null) {
				buf.release();
			}
		}
	}

	public void handle(Prop prop, Context ctx, byte[] data) {
		var buf0 = Unpooled.wrappedBuffer(data);

		try {
			var buf = PlatformHelper.CURRENT.createBuffer(buf0, prop.level.registryAccess());
			var payload = streamCodec.decode(buf);
			handler.handle(Cast.to(prop), ctx, payload);
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to handle a Prop packet", ex);
		} finally {
			buf0.release();
		}
	}
}
