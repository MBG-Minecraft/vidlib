package dev.latvian.mods.vidlib.feature.npc;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class NPCTypeRecording<T> {
	public final NPCDataType<T> type;
	public T initial;
	public T latest;
	private final List<NPCUpdate<T>> updates;
	private NPCUpdate<T>[] updateArray;

	public NPCTypeRecording(NPCDataType<T> type) {
		this.type = type;
		this.latest = null;
		this.updates = new ArrayList<>();
		this.updateArray = null;
	}

	void record(long offset, float delta, Player player) {
		var value = type.getter().get(player, delta);

		if (initial == null) {
			initial = value;
		}

		if (latest == null) {
			latest = value;
		}

		if (latest != value && !type.isSimilar(value, latest)) {
			latest = value;
			updates.add(new NPCUpdate<>(offset, value));
			updateArray = null;
		}
	}

	public void read(RegistryFriendlyByteBuf buf) {
		initial = type.read(buf);
		int size = VarInt.read(buf);

		for (int i = 0; i < size; i++) {
			long offset = VarLong.read(buf);
			var value = type.read(buf);
			updates.add(new NPCUpdate<>(offset, value));
		}

		updateArray = null;
	}

	public void write(RegistryFriendlyByteBuf buf) {
		type.write(buf, initial);
		VarInt.write(buf, updates.size());

		for (var update : updates) {
			VarLong.write(buf, update.offset());
			type.write(buf, update.value());
		}
	}

	public T get(long offset) {
		if (updates.isEmpty()) {
			return initial;
		}

		if (updateArray == null) {
			var list = new ArrayList<NPCUpdate<T>>();
			list.add(new NPCUpdate<>(0L, initial));
			list.addAll(updates);
			updateArray = list.reversed().toArray(new NPCUpdate[0]);
		}

		for (var entry : updateArray) {
			if (offset >= entry.offset()) {
				return entry.value();
			}
		}

		return initial;
	}

	@Override
	public String toString() {
		return "NPCTypeRecording[type=" + type.name() + ", initial=" + initial + ", updates=" + updates.size() + "]";
	}
}