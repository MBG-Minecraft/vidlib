package dev.latvian.mods.vidlib.feature.prop;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Map;

public record PropNetworkInfo(PropType<?> type, Map<PropData<?, ?>, PropDataEntry> data, Map<PropPacketType<?, ?>, PropPacketEntry> packets) {
	public static final Map<PropType<?>, PropNetworkInfo> SERVER = new Reference2ObjectOpenHashMap<>();
	public static final Map<PropType<?>, PropNetworkInfo> CLIENT = new Reference2ObjectOpenHashMap<>();

	public PropNetworkInfo(PropType<?> type) {
		this(type, new Reference2ObjectOpenHashMap<>(), new Reference2ObjectOpenHashMap<>());

		for (var entry : type.unsortedData()) {
			data.put(entry, new PropDataEntry(0, entry));
		}

		for (var entry : type.packets()) {
			packets.put(entry.packet(), new PropPacketEntry(0, entry.packet()));
		}

		/*
		var sortedDataList = type.unsortedData().stream().sorted(PropData.COMPARATOR).toList();
		var sortedPacketList = List.copyOf(type.packetSet());

		for (int i = 0; i < sortedDataList.size(); i++) {
			var p = new PropDataEntry(i, sortedDataList.get(i));
			data.put(p.data(), p);
		}

		for (int i = 0; i < sortedPacketList.size(); i++) {
			var p = new PropPacketEntry(i, sortedPacketList.get(i));
			packets.put(p.packet(), p);
		}
		 */
	}
}
