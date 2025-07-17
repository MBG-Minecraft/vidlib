package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.data.JOMLDataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.shape.ColoredShape;
import dev.latvian.mods.klib.shape.CuboidShape;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.math.kvector.FixedKVector;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.PositionType;
import imgui.ImGui;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
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
	public static final PropData<Prop, Boolean> CAN_COLLIDE = PropData.create(Prop.class, "can_collide", DataTypes.BOOL, p -> p.canCollide, (p, v) -> p.canCollide = v);
	public static final PropData<Prop, Boolean> CAN_INTERACT = PropData.create(Prop.class, "can_interact", DataTypes.BOOL, p -> p.canInteract, (p, v) -> p.canInteract = v);

	public static final PropData<Prop, KVector> DYNAMIC_POSITION = PropData.create(Prop.class, "position", KVector.DATA_TYPE, p -> p.dynamicPos == null ? KVector.of(p.pos) : p.dynamicPos, (p, v) -> {
		p.dynamicPos = v;

		if (v instanceof FixedKVector f) {
			p.setPos(f.vec());
		}
	});

	public final PropType<?> type;
	public final PropSpawnType spawnType;
	public final long createdTime;
	final Set<PropType.PropDataEntry> sync;
	public Level level;
	public int id;
	PropRemoveType removed;

	public int prevTick;
	public int tick;
	public int lifespan;
	public final Vector3d pos;
	public KVector dynamicPos;
	public final Vector3d prevPos;
	public final Vector3f velocity;
	public final Vector3f rotation;
	public final Vector3f prevRotation;
	public final Vector3f velocityMultiplier;
	public float gravity;
	public double width;
	public double height;
	public boolean canCollide;
	public boolean canInteract;

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
		this.dynamicPos = null;
		this.gravity = 0.08F;
		this.width = 1D;
		this.height = 1D;
		this.canCollide = false;
		this.canInteract = false;
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
		if (data.sync()) {
			var entry = type.reverseData().get(data);

			if (entry != null) {
				sync.add(entry);
			}
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
		var syncSet = allData ? type.data() : sync;

		if (syncSet.isEmpty()) {
			return null;
		}

		var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess(), ConnectionType.NEOFORGE);

		try {
			buf.writeVarInt(syncSet.size());

			for (var entry : syncSet) {
				var data = entry.data();
				var value = data.get(Cast.to(this));
				buf.writeVarInt(type.getDataIndex(data));
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
				var entry = type.getData(buf.readVarInt());

				if (entry != null) {
					var data = entry.data();
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

	public final void removeByCommand() {
		if (removed == PropRemoveType.NONE) {
			removed = PropRemoveType.COMMAND;
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

	public float getTick(float delta) {
		return Mth.lerp(delta, prevTick, tick);
	}

	public float getRelativeTick(float delta, float def) {
		return lifespan > 0 ? getTick(delta) / (float) lifespan : def;
	}

	public KNumberContext createWorldNumberContext() {
		var ctx = level.getGlobalContext().fork(getRelativeTick(1F, 1F), null);
		ctx.sourcePos = new Vec3(pos.x, pos.y, pos.z);
		return ctx;
	}

	public void move() {
		if (dynamicPos != null) {
			var ctx = createWorldNumberContext();
			var followPos = dynamicPos.get(ctx);

			if (followPos != null) {
				pos.set(followPos.x, followPos.y, followPos.z);
			}
		}

		pos.add(velocity);
		velocity.mul(velocityMultiplier);
		velocity.y -= gravity;
	}

	public void onAdded() {
		if (dynamicPos != null) {
			var ctx = createWorldNumberContext();
			var followPos = dynamicPos.get(ctx);

			if (followPos != null) {
				pos.set(followPos.x, followPos.y, followPos.z);
			}
		}
	}

	public void onRemoved() {
	}

	public void onExpired() {
	}

	public void onSpawned(CommandSourceStack source) {
	}

	public void save(DynamicOps<Tag> ops, CompoundTag nbt) {
		for (var entry : type.data()) {
			var p = entry.data();

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
			case SOUND_SOURCE -> getSoundSource(1F);
			case LOOK_TARGET -> new Vec3(pos.x, pos.y, pos.z).add(Rotation.deg(rotation.y, rotation.x, rotation.z).lookVec3(1D));
			default -> new Vec3(pos.x, pos.y, pos.z);
		};
	}

	public <T> void s2c(PropPacketType<?, T> type, T payload) {
		if (level.isServerSide()) {
			var packet = type.createPayload(this, payload);

			if (packet != null) {
				level.s2c(packet);
			}
		}
	}

	public void s2c(PropPacketType<?, Object> unitType) {
		s2c(unitType, PropPacketType.UNIT);
	}

	public <T> void c2s(PropPacketType<?, T> type, T payload) {
		if (level.isClientSide()) {
			var packet = type.createPayload(this, payload);

			if (packet != null) {
				level.c2s(packet);
			}
		}
	}

	public void c2s(PropPacketType<?, Object> unitType) {
		c2s(unitType, PropPacketType.UNIT);
	}

	public boolean canCollide(@Nullable Entity entity) {
		return true;
	}

	public boolean isCollidingWith(@Nullable Entity entity, AABB collisionBox) {
		if (!canCollide(entity)) {
			return false;
		}

		return collisionBox.intersects(pos.x - width / 2D, pos.y, pos.z - width / 2D, pos.x + width / 2D, pos.y + height, pos.z + width / 2D);
	}

	public void addCollisionShapes(@Nullable Entity entity, List<VoxelShape> shapes) {
		shapes.add(Shapes.create(pos.x - width / 2D, pos.y, pos.z - width / 2D, pos.x + width / 2D, pos.y + height, pos.z + width / 2D));
	}

	public boolean canInteract(@Nullable Entity entity) {
		return true;
	}

	public List<AABB> getClipBoxes(@Nullable Entity entity) {
		return List.of(new AABB(pos.x - width / 2D, pos.y, pos.z - width / 2D, pos.x + width / 2D, pos.y + height, pos.z + width / 2D));
	}

	@Nullable
	public PropHitResult clip(Line ray, ClipContext ctx) {
		var entity = ctx.getEntity();

		if (canInteract(entity)) {
			var hit = AABB.clip(getClipBoxes(entity), ray.start(), ray.end(), BlockPos.ZERO);

			if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
				return new PropHitResult(this, hit.getLocation(), hit.getDirection(), BlockPos.containing(hit.getLocation()), false);
			}
		}

		return null;
	}

	public void imgui(ImGraphics graphics, float delta) {
		var ops = graphics.mc.level.jsonOps();

		if (lifespan > 0) {
			ImGui.progressBar(getRelativeTick(delta, 0F), 0F, 20F);
		}

		for (var entry : type.data()) {
			try {
				ImGui.text(entry.data().key() + ": " + entry.data().type().codec().encodeStart(ops, Cast.to(entry.data().getter().apply(Cast.to(this)))).getOrThrow());
			} catch (Exception ex) {
				graphics.stackTrace(ex);
			}
		}
	}

	public void playSound(SoundData data, boolean looping, boolean stopImmediately) {
		level.playGlobalSound(new PositionedSoundData(data, this, looping, stopImmediately), KNumberVariables.EMPTY);
	}

	public final void playSound(SoundData data) {
		playSound(data, false, true);
	}

	public Vec3 getSoundSource(float delta) {
		return getPos(PositionType.EYES);
	}

	public BlockPos getBlockPos() {
		return BlockPos.containing(pos.x, pos.y, pos.z);
	}
}
