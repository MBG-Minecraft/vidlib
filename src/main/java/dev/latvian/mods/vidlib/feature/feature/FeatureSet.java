package dev.latvian.mods.vidlib.feature.feature;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

import java.util.stream.Collectors;

public class FeatureSet {
	public static final FeatureSet EMPTY = new FeatureSet(0);

	public static final StreamCodec<ByteBuf, FeatureSet> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public FeatureSet decode(ByteBuf buf) {
			int size = VarInt.read(buf);

			if (size == 0) {
				return EMPTY;
			}

			var set = new FeatureSet(size);

			for (int i = 0; i < size; i++) {
				var feature = Feature.STREAM_CODEC.decode(buf);
				var version = VarInt.read(buf);
				set.map.put(feature, version);
			}

			return set;
		}

		@Override
		public void encode(ByteBuf buf, FeatureSet value) {
			VarInt.write(buf, value.map.size());

			for (var entry : value.map.reference2IntEntrySet()) {
				Feature.STREAM_CODEC.encode(buf, entry.getKey());
				VarInt.write(buf, entry.getIntValue());
			}
		}
	};

	public static final Lazy<FeatureSet> SERVER_FEATURES = Lazy.of(() -> {
		var map = new Reference2IntOpenHashMap<Feature>();
		CommonGameEngine.INSTANCE.collectServerFeatures(map);
		return new FeatureSet(map);
	});

	public static final Lazy<FeatureSet> CLIENT_FEATURES = Lazy.of(() -> {
		var map = new Reference2IntOpenHashMap<Feature>();
		ClientGameEngine.INSTANCE.collectClientFeatures(map);
		return new FeatureSet(map);
	});

	public static FeatureSet REMOTE_SERVER_FEATURES = FeatureSet.EMPTY;

	private final Reference2IntMap<Feature> map;

	private FeatureSet(int size) {
		this.map = new Reference2IntOpenHashMap<>(size);
	}

	private FeatureSet(Reference2IntMap<Feature> map) {
		this.map = new Reference2IntOpenHashMap<>(map);
	}

	public int get(Feature feature) {
		return map.getOrDefault(feature, 0);
	}

	public boolean has(Feature feature) {
		return get(feature) > 0;
	}

	public boolean is(Feature feature, int version) {
		return get(feature) >= version;
	}

	@Override
	public String toString() {
		return map.keySet().stream().map(f -> f.id.toString()).collect(Collectors.joining(",", "FeatureSet[", "]"));
	}

	public Reference2IntMap<Feature> getAll() {
		return Reference2IntMaps.unmodifiable(map);
	}
}
