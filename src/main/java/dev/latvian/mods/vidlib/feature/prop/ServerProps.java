package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class ServerProps extends Props<ServerLevel> {
	private final List<Prop> pending;
	private final IntList removed;
	private int nextId;

	public ServerProps(ServerLevel level) {
		super(level);
		this.pending = new ArrayList<>();
		this.removed = new IntArrayList();
		this.nextId = 0;
	}

	@Override
	public void tick() {
		super.tick();

		var updates = new S2CPacketBundleBuilder(level);

		for (var prop : active.values()) {
			var update = prop.getDataUpdates(false);

			if (update != null) {
				updates.s2c(new UpdatePropPayload(prop.id, update));
			}
		}

		if (!pending.isEmpty()) {
			for (var prop : pending) {
				if (prop.spawnType.generateId()) {
					prop.id = ++nextId;
				}

				super.add(prop);

				for (var data : prop.type.data().values()) {
					prop.sync(data);
				}

				updates.s2c(prop.createAddPacket());
			}

			pending.clear();
		}

		if (!removed.isEmpty()) {
			updates.s2c(new RemovePropsPayload(new IntArrayList(removed)));
			removed.clear();
		}

		updates.send(level);
	}

	@Override
	public void add(Prop prop) {
		if (isValid(prop.spawnType)) {
			pending.add(prop);
		}
	}

	@Override
	protected void onRemoved(Prop prop) {
		if (prop.id != 0) {
			removed.add(prop.id);
		}
	}

	@Override
	protected boolean isValid(PropSpawnType type) {
		return type != PropSpawnType.DUMMY && type != PropSpawnType.ASSETS;
	}
}
