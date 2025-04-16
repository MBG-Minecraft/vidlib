package dev.latvian.mods.vidlib.core.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Function;

@Mixin(IdDispatchCodec.class)
public class IdDispatchCodecMixin<B extends ByteBuf, V, T> {
	@Shadow
	@Final
	private Function<V, ? extends T> typeGetter;

	@Shadow
	@Final
	private Object2IntMap<T> toId;

	@Shadow
	@Final
	private List<IdDispatchCodec.Entry<B, V, T>> byId;

	/**
	 * @author Lat
	 * @reason Better error messages
	 */
	@Overwrite
	public void encode(B buffer, V value) {
		var t = this.typeGetter.apply(value);
		int i = this.toId.getOrDefault(t, -1);

		if (i == -1) {
			throw new EncoderException("Sending unknown packet '" + t + "': " + value);
		}

		VarInt.write(buffer, i);
		IdDispatchCodec.Entry<B, V, T> entry = this.byId.get(i);

		try {
			StreamCodec<? super B, V> streamcodec = (StreamCodec<? super B, V>) entry.serializer();
			streamcodec.encode(buffer, value);
		} catch (Exception exception) {
			throw new EncoderException("Failed to encode packet '" + t + "': " + value, exception);
		}
	}
}
