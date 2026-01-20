package dev.mrbeastgaming.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Country(
	int id,
	String code,
	boolean hidden,
	String cca2,
	String cca3,
	int ccn3,
	String name,
	String nativeName,
	String displayName,
	String flagEmoji
) {
	public static final Codec<Country> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		API.HEX_ID_CODEC.fieldOf("id").forGetter(Country::id),
		Codec.STRING.optionalFieldOf("code", "").forGetter(Country::code),
		Codec.BOOL.optionalFieldOf("hidden", false).forGetter(Country::hidden),
		Codec.STRING.optionalFieldOf("cca2", "").forGetter(Country::cca2),
		Codec.STRING.optionalFieldOf("cca3", "").forGetter(Country::cca3),
		Codec.INT.optionalFieldOf("ccn3", 0).forGetter(Country::ccn3),
		Codec.STRING.optionalFieldOf("name", "").forGetter(Country::name),
		Codec.STRING.optionalFieldOf("native_name", "").forGetter(Country::nativeName),
		Codec.STRING.optionalFieldOf("display_name", "").forGetter(Country::displayName),
		Codec.STRING.optionalFieldOf("flag_emoji", "").forGetter(Country::flagEmoji)
	).apply(instance, Country::new));

	public static final StreamCodec<ByteBuf, Country> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.INT, Country::id,
		ByteBufCodecs.STRING_UTF8, Country::code,
		ByteBufCodecs.BOOL, Country::hidden,
		ByteBufCodecs.STRING_UTF8, Country::cca2,
		ByteBufCodecs.STRING_UTF8, Country::cca3,
		ByteBufCodecs.VAR_INT, Country::ccn3,
		ByteBufCodecs.STRING_UTF8, Country::name,
		ByteBufCodecs.STRING_UTF8, Country::nativeName,
		ByteBufCodecs.STRING_UTF8, Country::displayName,
		ByteBufCodecs.STRING_UTF8, Country::flagEmoji,
		Country::new
	);

	@Override
	public String displayName() {
		if (!displayName.isEmpty()) {
			return displayName;
		}

		return nativeName.isEmpty() ? name : (name + " (" + nativeName + ")");
	}
}
