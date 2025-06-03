package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.vidlib.feature.codec.DataType;
import dev.latvian.mods.vidlib.feature.codec.JOMLDataTypes;
import dev.latvian.mods.vidlib.util.Cast;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Set;

public class Prop {
	public static final PropData<Prop, Integer> TICK = PropData.create(Prop.class, "tick", DataType.VAR_INT, p -> p.tick, (p, v) -> p.tick = v);
	public static final PropData<Prop, Integer> LIFESPAN = PropData.create(Prop.class, "lifespan", DataType.VAR_INT, p -> p.lifespan, (p, v) -> p.lifespan = v);
	public static final PropData<Prop, Vector3d> POSITION = PropData.create(Prop.class, "position", JOMLDataTypes.DVEC_3, p -> p.pos, (p, v) -> p.pos.set(v));
	public static final PropData<Prop, Vector3f> VELOCITY = PropData.create(Prop.class, "velocity", JOMLDataTypes.VEC_3, p -> p.velocity, (p, v) -> p.velocity.set(v));
	public static final PropData<Prop, Vector3f> ROTATION = PropData.create(Prop.class, "rotation", JOMLDataTypes.VEC_3, p -> p.rotation, (p, v) -> p.rotation.set(v));
	public static final PropData<Prop, Float> GRAVITY = PropData.create(Prop.class, "gravity", DataType.FLOAT, p -> p.gravity, (p, v) -> p.gravity = v);
	public static final PropData<Prop, Float> WIDTH = PropData.create(Prop.class, "width", DataType.FLOAT, p -> (float) p.width, (p, v) -> p.width = v);
	public static final PropData<Prop, Float> HEIGHT = PropData.create(Prop.class, "height", DataType.FLOAT, p -> (float) p.height, (p, v) -> p.height = v);

	public static final PropDataProvider BUILTIN_DATA = PropDataProvider.join(
		TICK,
		POSITION,
		VELOCITY,
		ROTATION
	);

	public final PropType<?> type;
	public final PropSpawnType spawnType;
	final Set<PropData<?, ?>> sync;
	public Level level;
	public int id;
	public long uid;
	boolean removed;
	Object cachedRenderer;

	public int tick;
	public int lifespan;
	public final Vector3d pos;
	public final Vector3d prevPos;
	public final Vector3f velocity;
	public final Vector3f rotation;
	public final Vector3f prevRotation;
	public final Vector3f velocityMultiplier;
	public float gravity;
	public double width;
	public double height;

	public Prop(PropContext<?> ctx) {
		this.type = ctx.type();
		this.spawnType = ctx.spawnType();
		this.sync = new ReferenceArraySet<>();
		this.id = 0;
		this.uid = 0L;
		this.removed = false;
		this.tick = 0;
		this.lifespan = 0;
		this.pos = new Vector3d();
		this.prevPos = new Vector3d();
		this.velocity = new Vector3f();
		this.rotation = new Vector3f();
		this.prevRotation = new Vector3f();
		this.velocityMultiplier = new Vector3f(0.98F, 1F, 0.98F);
		this.gravity = 0.08F;
		this.width = 1D;
		this.height = 1D;
	}

	public final boolean fullTick() {
		snap();
		tick();

		if (isRemoved()) {
			onRemoved();
			level.getProps().onRemoved(this);
			return true;
		}

		tick++;
		return false;
	}

	public final void sync(PropData<?, ?> data) {
		if (data.sync() && type.reverseIdMap().containsKey(data)) {
			sync.add(data);
		}
	}

	public void syncMovement() {
		sync(POSITION);
		sync(VELOCITY);
		sync(ROTATION);
		sync(GRAVITY);
	}

	byte[] consumeUpdates() {
		if (sync.isEmpty()) {
			return null;
		}

		var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess(), ConnectionType.NEOFORGE);

		try {
			buf.writeVarInt(sync.size());

			for (var data : sync) {
				var value = data.get(Cast.to(this));
				buf.writeVarInt(type.reverseIdMap().getInt(data));
				data.type().streamCodec().encode(buf, Cast.to(value));
			}

			return buf.array();
		} finally {
			buf.release();
			sync.clear();
		}
	}

	void update(RegistryAccess registryAccess, byte[] update) {
		var buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(update), registryAccess, ConnectionType.NEOFORGE);

		try {
			int size = buf.readVarInt();

			for (int i = 0; i < size; i++) {
				var data = type.idMap().get(buf.readVarInt());

				if (data != null) {
					var value = data.type().streamCodec().decode(buf);
					data.set(Cast.to(this), Cast.to(value));
				}
			}
		} finally {
			buf.release();
		}
	}

	public void remove() {
		removed = true;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void snap() {
		prevPos.set(pos);
		prevRotation.set(rotation);
	}

	public void tick() {
		move();

		if (lifespan > 0 && tick >= lifespan) {
			onExpired();
			remove();
		}
	}

	public float getRelativeTick() {
		return lifespan > 0 ? tick / (float) lifespan : 0F;
	}

	public void move() {
		pos.add(velocity);
		velocity.mul(velocityMultiplier);
		velocity.y -= gravity;
	}

	public void onAdded() {
	}

	public void onRemoved() {
	}

	public void onExpired() {
	}

	public void onSpawned(CommandSourceStack source) {
	}

	public void save(DynamicOps<Tag> ops, CompoundTag nbt) {
		for (var p : type.data().values()) {
			if (p.save()) {
				nbt.put(p.key(), p.type().codec().encodeStart(ops, Cast.to(p.get(Cast.to(this)))).getOrThrow());
			}
		}
	}

	@Override
	public String toString() {
		return "%s#%08X/%d".formatted(type.id(), uid, id);
	}
}
