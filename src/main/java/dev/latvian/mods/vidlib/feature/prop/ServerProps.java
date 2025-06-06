package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class ServerProps extends Props<ServerLevel> {
	private final List<Prop> pending;

	public ServerProps(ServerLevel level) {
		super(level);
		this.pending = new ArrayList<>();
	}

	@Override
	public void tick() {
		super.tick();

		var updates = new S2CPacketBundleBuilder(level);

		for (var list : propLists.values()) {
			for (var prop : list) {
				var update = prop.isRemoved() ? null : prop.getDataUpdates(false);

				if (update != null) {
					updates.s2c(new UpdatePropPayload(prop.spawnType.listType, prop.id, update));
				}
			}

			if (!list.removed.isEmpty()) {
				updates.s2c(new RemovePropsPayload(list.type, new IntArrayList(list.removed)));
				list.removed.clear();
			}
		}

		if (!pending.isEmpty()) {
			for (var prop : pending) {
				super.add(prop);

				for (var data : prop.type.data().values()) {
					prop.sync(data);
				}

				updates.s2c(prop.createAddPacket());
			}

			pending.clear();
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
			propLists.get(prop.spawnType.listType).removed.add(prop.id);
		}
	}

	@Override
	protected boolean isValid(PropSpawnType type) {
		return type != PropSpawnType.DUMMY && type != PropSpawnType.ASSETS;
	}
}
