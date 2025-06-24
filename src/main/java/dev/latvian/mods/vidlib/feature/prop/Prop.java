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
import dev.latvian.mods.vidlib.math.worldvector.PositionType;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Position;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;

public class Prop {
	public static final PropData<Prop, Integer> TICK = PropData.create(Prop.class, "tick", DataTypes.TICKS, p -> p.tick, (p, v) -> p.tick = v);
	public static final PropData<Prop, Integer> LIFESPAN = PropData.create(Prop.class, "lifespan", DataTypes.TICKS, p -> p.lifespan, (p, v) -> p.lifespan = v);
	public static final PropData<Prop, Vector3d> POSITION = PropData.create(Prop.class, "position", JOMLDataTypes.DVEC3, p -> p.pos, Prop::setPos);
	public static final PropData<Prop, Vector3f> VELOCITY = PropData.create(Prop.class, "velocity", JOMLDataTypes.VEC3, p -> p.velocity, Prop::setVelocity);
	public static final PropData<Prop, Float> PITCH = PropData.create(Prop.class, "pitch", DataTypes.FLOAT, p -> p.rotation.x, Prop::setPitch);
	public static final PropData<Prop, Float> YAW = PropData.create(Prop.class, "yaw", DataTypes.FLOAT, p -> p.rotation.y, Prop::setYaw);
	public static final PropData<Prop, Float> ROLL = PropData.create(Prop.class, "roll", DataTypes.FLOAT, p -> p.rotation.z, Prop::setRoll);
	public static final PropData<Prop, Float> GRAVITY = PropData.create(Prop.class, "gravity", DataTypes.FLOAT, p -> p.gravity, (p, v) -> p.gravity = v);
	public static final PropData<Prop, Float> WIDTH = PropData.create(Prop.class, "width", DataTypes.FLOAT, p -> (float) p.width, (p, v) -> p.width = v);
	public static final PropData<Prop, Float> HEIGHT = PropData.create(Prop.class, "height", DataTypes.FLOAT, p -> (float) p.height, (p, v) -> p.height = v);

	public static final PropDataProvider BUILTIN_DATA = PropDataProvider.join(
		TICK,
		POSITION,
		VELOCITY,
		YAW,
		PITCH
	);

	public final PropType<?> type;
	public final PropSpawnType spawnType;
	public final long createdTime;
	final Set<PropData<?, ?>> sync;
	public Level level;
	public int id;
	PropRemoveType removed;

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
		this.removed = PropRemoveType.NONE;
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

	public final boolean isTimeTraveling(long time) {
		return createdTime > time || (lifespan > 0 && time > createdTime + lifespan + 20L);
	}

	public final boolean fullTick(long time) {
		if (isRemoved()) {
			return true;
		} else if (isTimeTraveling(time)) {
			removed = PropRemoveType.TIME_TRAVEL;
			return true;
		}

		snap();
		tick();

		if (lifespan > 0 && tick >= lifespan) {
			onExpired();
			removed = PropRemoveType.EXPIRED;
			return true;
		} else if (isRemoved()) {
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

	public final void setSize(double size) {
		width = size;
		height = size;
	}

	public void setPos(double x, double y, double z) {
		pos.set(x, y, z);
	}

	public Vec3 getPos(float delta) {
		return new Vec3(
			Mth.lerp(delta, prevPos.x, pos.x),
			Mth.lerp(delta, prevPos.y, pos.y),
			Mth.lerp(delta, prevPos.z, pos.z)
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
		rotation.set(pitch, yaw, roll);
	}

	public final void setYaw(float yaw) {
		setRot(yaw, rotation.x, rotation.z);
	}

	public final void setPitch(float pitch) {
		setRot(rotation.y, pitch, rotation.z);
	}

	public final void setRoll(float roll) {
		setRot(rotation.y, rotation.x, roll);
	}

	public final void setRot(Vector3fc rotation) {
		setRot(rotation.y(), rotation.x(), rotation.z());
	}

	public final void setRot(Rotation rotation) {
		setRot(rotation.yawDeg(), rotation.pitchDeg(), rotation.rollDeg());
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

			var bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			return bytes;
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
		if (removed == PropRemoveType.NONE) {
			removed = PropRemoveType.GAME;
		}
	}

	public final PropRemoveType getRemovedType() {
		return removed;
	}

	public final boolean isRemoved() {
		return removed != PropRemoveType.NONE;
	}

	public void snap() {
		prevTick = tick;
		prevPos.set(pos);
		prevRotation.set(rotation);
	}

	public void tick() {
		move();
	}

	public final float getRelativeTick() {
		return lifespan > 0 ? tick / (float) lifespan : 0F;
	}

	public final float getRelativeTick(float delta) {
		return lifespan > 0 ? Mth.lerp(delta, prevTick, tick) / (float) lifespan : 0F;
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

	public Component getDisplayName() {
		return Component.literal(type.translationKey());
	}

	public CommandSourceStack getCommandSourceAt(CommandSourceStack original) {
		return original.withAnchor(EntityAnchorArgument.Anchor.FEET)
			.withPosition(getPos(1F))
			.withRotation(new Vec2(getPitch(1F), getYaw(1F)));
	}

	public Vec3 getPos(PositionType type) {
		return switch (type) {
			case CENTER -> new Vec3(pos.x, pos.y + height / 2D, pos.z);
			case TOP -> new Vec3(pos.x, pos.y + height, pos.z);
			case EYES -> new Vec3(pos.x, pos.y + height * 0.75D, pos.z);
			case LEASH -> new Vec3(pos.x, pos.y + height * 2D / 5D, pos.z);
			case SOUND_SOURCE -> getPos(PositionType.EYES);
			case LOOK_TARGET -> new Vec3(pos.x, pos.y, pos.z).add(Rotation.deg(rotation.y, rotation.x, rotation.z).lookVec3(1D));
			default -> new Vec3(pos.x, pos.y, pos.z);
		};
	}
}
