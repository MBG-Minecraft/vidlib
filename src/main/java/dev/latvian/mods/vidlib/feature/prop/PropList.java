package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PropList implements Iterable<Prop> {
	public final Props<?> props;
	public final PropListType type;
	private final Int2ObjectMap<Prop> map;
	public final Map<PropRemoveType, IntList> removed;
	public final List<Prop> collidingProps;
	public final List<Prop> interactableProps;

	public PropList(Props<?> props, PropListType type) {
		this.props = props;
		this.type = type;
		this.map = new Int2ObjectLinkedOpenHashMap<>();
		this.removed = new EnumMap<>(PropRemoveType.class);
		this.collidingProps = new ArrayList<>(0);
		this.interactableProps = new ArrayList<>(0);

		for (var removeType : PropRemoveType.VALUES) {
			removed.put(removeType, new IntArrayList());
		}
	}

	private boolean fullTick(Prop prop) {
		if (prop.fullTick(props.level.getGameTime())) {
			prop.onRemoved();
			props.onRemoved(prop);
			removed.get(prop.removed).add(prop.id);
			return true;
		} else {
			if (prop.canCollide) {
				collidingProps.add(prop);
			}

			if (prop.canInteract) {
				interactableProps.add(prop);
			}
		}

		return false;
	}

	public void tick(@Nullable S2CPacketBundleBuilder updates) {
		collidingProps.clear();
		interactableProps.clear();

		if (!map.isEmpty()) {
			map.values().removeIf(this::fullTick);
		}

		if (updates != null) {
			for (var prop : map.values()) {
				var update = prop.createUpdatePacket();

				if (update != null) {
					prop.sync.clear();
					updates.s2c(update);
				}
			}
		}

		for (var entry : removed.entrySet()) {
			var list = entry.getValue();

			if (!list.isEmpty()) {
				if (updates != null) {
					updates.s2c(new RemovePropsPayload(type, new IntArrayList(list), entry.getKey()));
				}

				list.clear();
			}
		}
	}

	void add(Prop prop) {
		if (prop.id == 0) {
			prop.id = generateNewId();
		} else {
			var old = map.get(prop.id);

			if (old != null) {
				old.snap();
				old.remove(PropRemoveType.REPLACED);
			}
		}

		map.put(prop.id, prop);
		prop.onAdded();
		props.onAdded(prop);
		prop.snap();

		if (props.level.isServerSide()) {
			for (var entry : prop.type.data()) {
				prop.sync(entry.data());
			}

			props.level.s2c(prop.createAddPacket());
		}
	}

	public int removeAll(PropRemoveType removeType) {
		int count = map.size();

		for (var prop : map.values()) {
			prop.remove(removeType);
		}

		return count;
	}

	public int removeAll(PropRemoveType removeType, Predicate<Prop> predicate) {
		int count = 0;

		for (var prop : this) {
			if (predicate.test(prop)) {
				prop.remove(removeType);
				count++;
			}
		}

		return count;
	}

	public int generateNewId() {
		int id = 0;

		while (id <= 0 || map.containsKey(id)) {
			id = props.level.random.nextInt(32768, Integer.MAX_VALUE);
		}

		return id;
	}

	@Nullable
	public Prop get(int id) {
		return map.get(id);
	}

	@Override
	public @NotNull Iterator<Prop> iterator() {
		return map.values().iterator();
	}

	public Collection<Prop> all() {
		return map.values();
	}

	public int size() {
		return map.size();
	}

	public boolean intersectsSolid(@Nullable Entity entity, AABB collisionBox) {
		if (entity == null || collidingProps.isEmpty()) {
			return false;
		}

		for (var prop : collidingProps) {
			if (prop.isCollidingWith(entity, collisionBox)) {
				return true;
			}
		}

		return false;
	}

	public List<VoxelShape> getShapesIntersecting(@Nullable Entity entity, AABB collisionBox) {
		if (collidingProps.isEmpty()) {
			return List.of();
		}

		var shapes = List.<VoxelShape>of();

		for (var prop : collidingProps) {
			if (prop.isCollidingWith(entity, collisionBox)) {
				if (shapes.isEmpty()) {
					shapes = new ArrayList<>();
				}

				prop.addCollisionShapes(entity, shapes);
			}
		}

		return shapes;
	}

	@Nullable
	public PropHitResult clip(ClipContext ctx, boolean ignoreInteractable) {
		var props = ignoreInteractable ? all() : interactableProps;

		if (props.isEmpty()) {
			return null;
		}

		PropHitResult result = null;

		for (var prop : props) {
			var hit = prop.clip(ctx);

			if (hit != null) {
				if (result == null || hit.getLocation().distanceToSqr(ctx.getFrom()) < result.getLocation().distanceToSqr(ctx.getFrom())) {
					result = hit;
				}
			}
		}

		return result;
	}
}
