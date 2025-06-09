package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.data.JOMLDataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.shape.ColoredShape;
import dev.latvian.mods.klib.shape.CuboidShape;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Position;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;

public class Prop {
	public static final PropData<Prop, Integer> TICK = PropData.create(Prop.class, "tick", DataTypes.VAR_INT, p -> p.tick, (p, v) -> p.tick = v);
	public static final PropData<Prop, Integer> LIFESPAN = PropData.create(Prop.class, "lifespan", DataTypes.VAR_INT, p -> p.lifespan, (p, v) -> p.lifespan = v);
	public static final PropData<Prop, Vector3d> POSITION = PropData.create(Prop.class, "position", JOMLDataTypes.DVEC3, p -> p.pos, (p, v) -> p.pos.set(v));
	public static final PropData<Prop, Vector3f> VELOCITY = PropData.create(Prop.class, "velocity", JOMLDataTypes.VEC3, p -> p.velocity, (p, v) -> p.velocity.set(v));
	public static final PropData<Prop, Vector3f> ROTATION = PropData.create(Prop.class, "rotation", JOMLDataTypes.VEC3, p -> p.rotation, (p, v) -> p.rotation.set(v));
	public static final PropData<Prop, Float> GRAVITY = PropData.create(Prop.class, "gravity", DataTypes.FLOAT, p -> p.gravity, (p, v) -> p.gravity = v);
	public static final PropData<Prop, Float> WIDTH = PropData.create(Prop.class, "width", DataTypes.FLOAT, p -> (float) p.width, (p, v) -> p.width = v);
	public static final PropData<Prop, Float> HEIGHT = PropData.create(Prop.class, "height", DataTypes.FLOAT, p -> (float) p.height, (p, v) -> p.height = v);

	public static final PropDataProvider BUILTIN_DATA = PropDataProvider.join(
		TICK,
		POSITION,
		VELOCITY,
		ROTATION
	);

	public final PropType<?> type;
	public final PropSpawnType spawnType;
	public final long createdTime;
	final Set<PropData<?, ?>> sync;
	public Level level;
	public int id;
	boolean removed;
	Object cachedRenderer;

	public int prevTick;
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
		this.createdTime = ctx.createdTime();
		this.sync = new ReferenceArraySet<>();
		this.level = ctx.props().level;
		this.id = 0;
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

	public final boolean fullTick(long time) {
		if (isRemoved() || createdTime > time) {
			return true;
		}

		snap();
		tick();

		if (isRemoved()) {
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

	public void setPos(double x, double y, double z) {
		pos.set(x, y, z);
	}

	public Vec3 getPos(float delta) {
		return new Vec3(
			Mth.lerp(delta, prevPos.x, pos.x),
			Mth.lerp(delta, prevPos.x, pos.y),
			Mth.lerp(delta, prevPos.x, pos.z)
		);
	}

	public final void setPos(Vector3dc pos) {
		setPos(pos.x(), pos.y(), pos.z());
	}

	public final void setPos(Position pos) {
		setPos(pos.x(), pos.y(), pos.z());
	}

	public void setVelocity(float x, float y, float z) {
		velocity.set(x, y, z);
	}

	public final void setVelocity(Vector3fc velocity) {
		setVelocity(velocity.x(), velocity.y(), velocity.z());
	}

	public void setRot(float yaw, float pitch, float roll) {
		rotation.set(yaw, pitch, roll);
	}

	public float getPitch(float delta) {
		return Mth.rotLerp(delta, prevRotation.x, rotation.x);
	}

	public float getYaw(float delta) {
		return Mth.rotLerp(delta, prevRotation.y, rotation.y);
	}

	public float getRoll(float delta) {
		return Mth.rotLerp(delta, prevRotation.z, rotation.z);
	}

	public final void setRot(Vector3fc rotation) {
		setRot(rotation.x(), rotation.y(), rotation.z());
	}

	public final void setRot(Rotation rotation) {
		setRot(rotation.yawDeg(), rotation.pitchDeg(), rotation.rollDeg());
	}

	byte[] getDataUpdates(boolean allData) {
		var syncSet = allData ? type.data().values() : sync;

		if (syncSet.isEmpty()) {
			return null;
		}

		var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess(), ConnectionType.NEOFORGE);

		try {
			buf.writeVarInt(syncSet.size());

			for (var data : syncSet) {
				var value = data.get(Cast.to(this));
				buf.writeVarInt(type.reverseIdMap().getInt(data));
				data.type().streamCodec().encode(buf, Cast.to(value));
			}

			return buf.array();
		} finally {
			buf.release();

			if (!allData) {
				sync.clear();
			}
		}
	}

	void update(RegistryAccess registryAccess, byte[] update, boolean allData) {
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

	public final void remove() {
		removed = true;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void snap() {
		prevTick = tick;
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
		return "%s#%04X".formatted(type.id(), id);
	}

	public SimplePacketPayload createAddPacket() {
		return new AddPropPayload(this);
	}

	@Nullable
	public SimplePacketPayload createUpdatePacket() {
		return UpdatePropPayload.of(this);
	}

	public float getTick(float delta) {
		return Mth.lerp(delta, prevTick, tick);
	}

	public double getMaxRenderDistance() {
		return 8192D;
	}

	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		double w = width / 2D;
		return frustum.isVisible(x - w, y, z - w, x + w, y + height, z + w);
	}

	public Visuals getDebugVisuals(double x, double y, double z) {
		var visuals = new Visuals();
		visuals.add(new ColoredShape(new CuboidShape(Vec3f.of(width, height, width), Rotation.NONE), Color.TRANSPARENT, Color.WHITE).at(x, y + height / 2D, z));
		return visuals;
	}

	public float getDebugVisualsProgress(float delta) {
		return lifespan > 0 ? getTick(delta) / (float) lifespan : 1F;
	}

	public Entity asEntity() {
		return new PropEntity(this);
	}
}
