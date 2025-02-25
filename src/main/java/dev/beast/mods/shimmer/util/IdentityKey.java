package dev.beast.mods.shimmer.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.IdentityHashMap;
import java.util.Map;

public class IdentityKey {
	private static final Object LOCK = new Object();
	private static final Map<String, IdentityKey> MAP = new IdentityHashMap<>();
	public static final IdentityKey EMPTY = new IdentityKey("");

	public static final Codec<IdentityKey> CODEC = Codec.STRING.xmap(IdentityKey::create, IdentityKey::id);
	public static final StreamCodec<ByteBuf, IdentityKey> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(IdentityKey::create, IdentityKey::id);

	public static IdentityKey create(String id) {
		if (id.isEmpty()) {
			return EMPTY;
		}

		synchronized (LOCK) {
			return MAP.computeIfAbsent(id, IdentityKey::new);
		}
	}

	public final String id;

	private IdentityKey(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public String toString() {
		return id;
	}
}
