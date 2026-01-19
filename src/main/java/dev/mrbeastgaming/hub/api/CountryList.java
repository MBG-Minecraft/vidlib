package dev.mrbeastgaming.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;

public record CountryList(String checksum, Int2ObjectMap<Country> byId, Map<String, Country> byCode, Map<String, Country> byCCA2) {
	public static final CountryList EMPTY = new CountryList("", Int2ObjectMaps.emptyMap(), Map.of(), Map.of());

	private static CountryList of(String checksum, List<Country> list) {
		if (checksum.isEmpty() && list.isEmpty()) {
			return EMPTY;
		}

		var byId = new Int2ObjectLinkedOpenHashMap<Country>(list.size());
		var byCode = new Object2ObjectLinkedOpenHashMap<String, Country>(list.size());
		var byCCA2 = new Object2ObjectLinkedOpenHashMap<String, Country>(list.size());

		for (var country : list) {
			byId.put(country.id(), country);
			byCode.put(country.code(), country);
			byCCA2.put(country.cca2(), country);
		}

		return new CountryList(checksum, byId, byCode, byCCA2);
	}

	public static final Codec<CountryList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("checksum", "").forGetter(CountryList::checksum),
		Country.CODEC.listOf().optionalFieldOf("countries", List.of()).forGetter(CountryList::countryList)
	).apply(instance, CountryList::of));

	public static final StreamCodec<ByteBuf, CountryList> STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.STRING_UTF8, CountryList::checksum,
		KLibStreamCodecs.listOf(Country.STREAM_CODEC), CountryList::countryList,
		CountryList::of
	);

	private List<Country> countryList() {
		return List.copyOf(byCode.values());
	}
}
