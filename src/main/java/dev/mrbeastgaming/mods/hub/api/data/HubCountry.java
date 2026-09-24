package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
) implements HubDBObject {
	public static final Codec<String> CODE_CODEC = Codec.STRING.validate(s -> {
		if (s.isEmpty()) {
			return DataResult.success("xx");
		} else if (s.length() == 2 && Character.isLowerCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
			return DataResult.success(s);
		} else {
			return DataResult.error(() -> "Invalid country code: " + s);
		}
	});

	public static final Codec<HubCountry> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HubDBObject.ID_FIELD.forGetter(HubCountry::id),
		CODE_CODEC.optionalFieldOf("code", "xx").forGetter(HubCountry::code),
		Codec.BOOL.optionalFieldOf("hidden", false).forGetter(HubCountry::hidden),
		Codec.STRING.optionalFieldOf("cca2", "XX").forGetter(HubCountry::cca2),
		Codec.STRING.optionalFieldOf("cca3", "XXX").forGetter(HubCountry::cca3),
		Codec.INT.optionalFieldOf("ccn3", 0).forGetter(HubCountry::ccn3),
		Codec.STRING.optionalFieldOf("name", "Unknown").forGetter(HubCountry::name),
		Codec.STRING.optionalFieldOf("native_name", "Unknown").forGetter(HubCountry::nativeName),
		Codec.STRING.optionalFieldOf("display_name", "Unknown").forGetter(HubCountry::displayName),
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

	public static final Codec<HubCountry> CODEC = HubResponseContext.resolvingCodec(DIRECT_CODEC, CODE_CODEC, HubCountry::code, HubResponseContext::country);

	@Override
	public String displayName() {
		if (!displayName.isEmpty()) {
			return displayName;
		}

		return nativeName.isEmpty() ? name : (name + " (" + nativeName + ")");
	}
}
