package dev.beast.mods.shimmer.feature.prop;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class ServerPropList extends PropList<ServerLevel> {
	private final List<Prop> added;
	private final IntList removed;
	private int nextId;

	public ServerPropList(ServerLevel level) {
		super(level);
		this.added = new ArrayList<>();
		this.removed = new IntArrayList();
		this.nextId = 0;
	}

	@Override
	public void tick() {
		super.tick();

		if (!added.isEmpty()) {
			active.addAll(added);

			for (var prop : added) {
				prop.level = level;
				prop.onAdded();
				prop.id = ++nextId;
			}
		}

		if (!added.isEmpty() || !removed.isEmpty()) {
			added.clear();
			removed.clear();
		}
	}

	@Override
	public void add(Prop prop) {
		added.add(prop);
	}

	@Override
	protected void onRemoved(Prop prop) {
		removed.add(prop.id);
	}
}
