package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.io.checksum.Checksum;
import dev.latvian.mods.klib.io.checksum.NoChecksum;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Map;

public record HubCountryList(
	Checksum checksum,
	Int2ObjectMap<HubCountry> byId,
	Map<String, HubCountry> byCode,
	Map<String, HubCountry> byCCA2
) {
	public static final HubCountryList EMPTY = new HubCountryList(NoChecksum.INSTANCE, Int2ObjectMaps.emptyMap(), Map.of(), Map.of());

	private static HubCountryList of(Checksum checksum, List<HubCountry> list) {
		if (checksum.isNil() && list.isEmpty()) {
			return EMPTY;
		}

		var byId = new Int2ObjectLinkedOpenHashMap<HubCountry>(list.size());
		var byCode = new Object2ObjectLinkedOpenHashMap<String, HubCountry>(list.size());
		var byCCA2 = new Object2ObjectLinkedOpenHashMap<String, HubCountry>(list.size());

		for (var country : list) {
			byId.put(country.id().raw(), country);
			byCode.put(country.code(), country);
			byCCA2.put(country.cca2(), country);
		}

		return new HubCountryList(checksum, byId, byCode, byCCA2);
	}

	public static final Codec<HubCountryList> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Checksum.CODEC.optionalFieldOf("checksum", NoChecksum.INSTANCE).forGetter(HubCountryList::checksum),
		HubCountry.CODEC.listOf().optionalFieldOf("countries", List.of()).forGetter(HubCountryList::countryList)
	).apply(instance, HubCountryList::of));

	public static final StreamCodec<ByteBuf, HubCountryList> STREAM_CODEC = CompositeStreamCodec.of(
		Checksum.STREAM_CODEC, HubCountryList::checksum,
		KLibStreamCodecs.listOf(HubCountry.STREAM_CODEC), HubCountryList::countryList,
		HubCountryList::of
	);

	private List<HubCountry> countryList() {
		return List.copyOf(byCode.values());
	}
}
