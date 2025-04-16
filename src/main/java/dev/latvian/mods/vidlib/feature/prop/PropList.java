package dev.latvian.mods.vidlib.feature.prop;

import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public abstract class PropList<L extends Level> {
	public final L level;
	public final List<Prop> active;

	public PropList(L level) {
		this.level = level;
		this.active = new ArrayList<>();
	}

	public void tick() {
		var itr = active.iterator();

		while (itr.hasNext()) {
			var prop = itr.next();
			prop.updatePrevious();
			prop.tick();

			if (prop.removed) {
				prop.onRemoved();
				itr.remove();
				onRemoved(prop);
			}

			prop.tick++;
		}
	}

	protected void onRemoved(Prop prop) {
	}

	public void add(Prop prop) {
		throw new IllegalStateException("Can't add props on client side!");
	}
}
