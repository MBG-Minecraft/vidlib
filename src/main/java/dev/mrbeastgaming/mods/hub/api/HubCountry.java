package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.Hex32;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HubCountry(
	Hex32 id,
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
	public static final Codec<HubCountry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Hex32.CODEC.fieldOf("id").forGetter(HubCountry::id),
		Codec.STRING.optionalFieldOf("code", "").forGetter(HubCountry::code),
		Codec.BOOL.optionalFieldOf("hidden", false).forGetter(HubCountry::hidden),
		Codec.STRING.optionalFieldOf("cca2", "").forGetter(HubCountry::cca2),
		Codec.STRING.optionalFieldOf("cca3", "").forGetter(HubCountry::cca3),
		Codec.INT.optionalFieldOf("ccn3", 0).forGetter(HubCountry::ccn3),
		Codec.STRING.optionalFieldOf("name", "").forGetter(HubCountry::name),
		Codec.STRING.optionalFieldOf("native_name", "").forGetter(HubCountry::nativeName),
		Codec.STRING.optionalFieldOf("display_name", "").forGetter(HubCountry::displayName),
		Codec.STRING.optionalFieldOf("flag_emoji", "").forGetter(HubCountry::flagEmoji)
	).apply(instance, HubCountry::new));

	public static final StreamCodec<ByteBuf, HubCountry> STREAM_CODEC = CompositeStreamCodec.of(
		Hex32.STREAM_CODEC, HubCountry::id,
		ByteBufCodecs.STRING_UTF8, HubCountry::code,
		ByteBufCodecs.BOOL, HubCountry::hidden,
		ByteBufCodecs.STRING_UTF8, HubCountry::cca2,
		ByteBufCodecs.STRING_UTF8, HubCountry::cca3,
		ByteBufCodecs.VAR_INT, HubCountry::ccn3,
		ByteBufCodecs.STRING_UTF8, HubCountry::name,
		ByteBufCodecs.STRING_UTF8, HubCountry::nativeName,
		ByteBufCodecs.STRING_UTF8, HubCountry::displayName,
		ByteBufCodecs.STRING_UTF8, HubCountry::flagEmoji,
		HubCountry::new
	);

	@Override
	public String displayName() {
		if (!displayName.isEmpty()) {
			return displayName;
		}

		return nativeName.isEmpty() ? name : (name + " (" + nativeName + ")");
	}
}
