package dev.beast.mods.shimmer.feature.zone;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ZoneInstance {
	public int index;
	public Zone zone;
	public final IntSet entities;
	public Object renderer;

	public ZoneInstance(Zone zone) {
		this.index = -1;
		this.zone = zone;
		this.entities = new IntOpenHashSet();
		this.renderer = null;
	}

	public boolean contains(Entity entity) {
		return entities.contains(entity.getId());
	}

	public void tick(Level level) {
	}
}
