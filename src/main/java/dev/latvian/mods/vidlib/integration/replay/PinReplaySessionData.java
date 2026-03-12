package dev.latvian.mods.vidlib.integration.replay;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.replay.api.ReplaySessionData;
import dev.latvian.mods.replay.api.ReplaySessionDataType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.pin.Pin;
import dev.latvian.mods.vidlib.feature.pin.Pins;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PinReplaySessionData implements ReplaySessionData {
	public static final ReplaySessionDataType<PinReplaySessionData> TYPE = new ReplaySessionDataType<>(VidLib.id("pins"), PinReplaySessionData::new);

	private static final Codec<Pair<UUID, Pin>> OLD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.UUID.fieldOf("uuid").forGetter(Pair::getFirst),
		Pin.MAP_CODEC.forGetter(Pair::getSecond)
	).apply(instance, Pair::of));

	private static final Codec<Map<UUID, Pin>> UNBOUND_MAP_CODEC = Codec.unboundedMap(KLibCodecs.UUID, Pin.CODEC);

	public static final Codec<Map<UUID, Pin>> CODEC = KLibCodecs.or(UNBOUND_MAP_CODEC, OLD_CODEC.listOf().xmap(list -> {
		var map = new HashMap<UUID, Pin>();

		for (var entry : list) {
			map.put(entry.getFirst(), entry.getSecond());
		}

		return map;
	}, map -> {
		var list = new ArrayList<Pair<UUID, Pin>>();

		for (var entry : map.entrySet()) {
			list.add(Pair.of(entry.getKey(), entry.getValue()));
		}

		return list;
	}));

	@Override
	public ReplaySessionDataType<?> getType() {
		return TYPE;
	}

	@Override
	public <O> void load(ReplaySession session, DynamicOps<O> ops, @Nullable O input) {
		Pins.PINS.clear();

		if (input != null) {
			CODEC.parse(ops, input).ifSuccess(Pins.PINS::putAll);
		}
	}

	@Override
	public <O> DataResult<O> save(ReplaySession session, DynamicOps<O> ops) {
		return CODEC.encodeStart(ops, Pins.PINS);
	}
}
