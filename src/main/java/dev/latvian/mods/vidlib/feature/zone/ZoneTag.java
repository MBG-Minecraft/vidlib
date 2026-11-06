package dev.latvian.mods.vidlib.feature.zone;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ZoneTag {
	private static final Interner<ZoneTag> INTERNER = Interners.newWeakInterner();

	public static ZoneTag of(String name) {
		return INTERNER.intern(new ZoneTag(name));
	}

	public static final Codec<ZoneTag> CODEC = Codec.STRING.xmap(ZoneTag::of, ZoneTag::toString);
	public static final StreamCodec<ByteBuf, ZoneTag> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ZoneTag::of, ZoneTag::toString);

	private final String name;

	private ZoneTag(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof ZoneTag t && name.equals(t.name);
	}

	public String toString() {
		return name;
	}
}
