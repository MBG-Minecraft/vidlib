package dev.beast.mods.shimmer.feature.zone;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class ZoneInstance {
	public int index;
	public Zone zone;
	public final Int2ObjectMap<Entity> entities;
	public Object renderer;

	public ZoneInstance(Zone zone) {
		this.index = -1;
		this.zone = zone;
		this.entities = new Int2ObjectOpenHashMap<>();
		this.renderer = null;
	}

	public boolean contains(Entity entity) {
		return entities.containsKey(entity.getId());
	}

	public void tick(@Nullable Level level) {
		var oldEntities = new Int2ObjectOpenHashMap<>(entities);
		entities.clear();

		if (level != null) {
			for (var entity : level.getEntities((Entity) null, zone.shape().getBoundingBox(), EntitySelector.ENTITY_STILL_ALIVE)) {
				if (zone.shape().contains(entity.getBoundingBox())) {
					entities.put(entity.getId(), entity);

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
}
