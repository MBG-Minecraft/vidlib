package dev.latvian.mods.vidlib.feature.zone;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class Zone {
	public final ZoneContainer container;
	public int index;
	public ZoneVolume volume;
	public final Int2ObjectMap<Entity> entities;
	public Object renderer;
	public final Set<String> tags;

	public Zone(ZoneContainer container, ZoneVolume volume) {
		this.container = container;
		this.index = -1;
		this.volume = volume;
		this.entities = new Int2ObjectOpenHashMap<>();
		this.renderer = null;
		this.tags = new LinkedHashSet<>();
	}

	public boolean has(Entity entity) {
		return entities.containsKey(entity.getId());
	}

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}

	public void tick(@Nullable Level level) {
		var oldEntities = new Int2ObjectOpenHashMap<>(entities);
		entities.clear();

		if (level != null) {
			for (var entity : volume.shape().collectEntities(level, volume.entityFilter())) {
				if (entity.isAlive() && volume.shape().intersects(entity.getBoundingBox())) {
					entities.put(entity.getId(), entity);

					var list = container.entityZones.get(entity.getId());

					if (list == null) {
						list = new ArrayList<>(1);
						container.entityZones.put(entity.getId(), list);
					}

					list.add(this);

					if (!oldEntities.containsKey(entity.getId())) {
						entityEntered(level, entity);
					}
				}
			}
		}

		for (var old : oldEntities.values()) {
			if (!entities.containsKey(old.getId())) {
				entityExited(level, old);
			}
		}
	}

	public void entityEntered(Level level, Entity entity) {
		NeoForge.EVENT_BUS.post(new ZoneEvent.EntityEntered(this, level, entity));
	}

	public void entityExited(Level level, Entity entity) {
		NeoForge.EVENT_BUS.post(new ZoneEvent.EntityExited(this, level, entity));
	}

	public void updateZone(ZoneVolume newVolume) {
		volume = newVolume;
		renderer = null;
	}
}
