package dev.latvian.mods.vidlib.feature.zone;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class ZoneInstance {
	public final ZoneContainer container;
	public UUID uuid;
	public int index;
	public Zone zone;
	public final Int2ObjectMap<Entity> entities;
	public Object renderer;
	public final Set<String> tags;

	public ZoneInstance(ZoneContainer container, Zone zone) {
		this.container = container;
		this.uuid = Util.NIL_UUID;
		this.index = -1;
		this.zone = zone;
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
			for (var entity : zone.shape().collectEntities(level, zone.entityFilter())) {
				if (entity.isAlive() && zone.shape().intersects(entity.getBoundingBox())) {
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
}
