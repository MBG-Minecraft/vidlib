package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.klib.block.BlockUtils;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.data.JOMLDataTypes;
import dev.latvian.mods.klib.entity.EntityUtils;
import dev.latvian.mods.klib.entity.PlayerTarget;
import dev.latvian.mods.klib.entity.PositionType;
import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.shape.ColoredShape;
import dev.latvian.mods.klib.shape.CuboidShape;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.VLFeatures;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Prop {
	public static final PropData<Prop, Integer> TICK = PropData.create(Prop.class, "tick", DataTypes.TICKS, p -> p.tick, (p, v) -> p.tick = v);
	public static final PropData<Prop, Integer> LIFESPAN = PropData.create(Prop.class, "lifespan", DataTypes.TICKS, p -> p.lifespan, (p, v) -> p.lifespan = v);
	public static final PropData<Prop, Vector3d> POSITION = PropData.create(Prop.class, "position", JOMLDataTypes.DVEC3, p -> p.pos, Prop::setPos);
	public static final PropData<Prop, Float> PITCH = PropData.create(Prop.class, "pitch", DataTypes.FLOAT, p -> p.rotation.x, Prop::setPitch);
	public static final PropData<Prop, Float> YAW = PropData.create(Prop.class, "yaw", DataTypes.FLOAT, p -> p.rotation.y, Prop::setYaw);
	public static final PropData<Prop, Float> ROLL = PropData.create(Prop.class, "roll", DataTypes.FLOAT, p -> p.rotation.z, Prop::setRoll);
	public static final PropData<Prop, Float> WIDTH = PropData.create(Prop.class, "width", DataTypes.FLOAT, p -> (float) p.width, (p, v) -> p.setWidth(v));
	public static final PropData<Prop, Float> HEIGHT = PropData.create(Prop.class, "height", DataTypes.FLOAT, p -> (float) p.height, (p, v) -> p.setHeight(v));
	public static final PropData<Prop, Boolean> CAN_COLLIDE = PropData.createBoolean(Prop.class, "can_collide", p -> p.canCollide, (p, v) -> p.canCollide = v);
	public static final PropData<Prop, Boolean> CAN_INTERACT = PropData.createBoolean(Prop.class, "can_interact", p -> p.canInteract, (p, v) -> p.canInteract = v);
	public static final PropData<Prop, Boolean> PAUSED = PropData.createBoolean(Prop.class, "paused", p -> p.paused, (p, v) -> p.paused = v);

	public final Props<?> props;
	public final PropType<?> type;
	public final PropSpawnType spawnType;
	public final long createdTime;
	final Set<PropDataEntry> sync;
	public final Level level;
	public int id;
	PropRemoveType removed;
	Map<PropData<?, ?>, Object> defaultValues = Map.of();
	public boolean clientSideOnly;
	Object ui;

	public int prevTick;
	public int tick;
	public int lifespan;
	public final Vector3d pos;
	public final Vector3d prevPos;
	public final Vector3f rotation;
	public final Vector3f prevRotation;
	public double width;
	public double height;
	public boolean canCollide;
	public boolean canInteract;
	public boolean paused;
	public long lastPlayerCollisionTime = -1L;

	public Prop(PropContext<?> ctx) {
		this.props = ctx.props();
		this.type = ctx.type();
		this.spawnType = ctx.spawnType();
		this.createdTime = ctx.createdTime();
		this.sync = new ReferenceArraySet<>();
		this.level = props.level;
		this.id = 0;
		this.removed = PropRemoveType.NONE;
		this.tick = 0;
		this.lifespan = 0;
		this.pos = new Vector3d();
		this.prevPos = new Vector3d();
		this.rotation = new Vector3f();
		this.prevRotation = new Vector3f();
		// this.velocityMultiplier = new Vector3f(0.98F, 1F, 0.98F);
		// this.gravity = 0.08F;
		this.width = 1D;
		this.height = 1D;
		this.canCollide = false;
		this.canInteract = false;
		this.paused = false;
	}

	public <T> T getData(PropData<?, T> data) {
		return data.getter().apply(Cast.to(this));
	}

	public <T> void setData(PropData<?, T> data, T value) {
		data.setter().accept(Cast.to(this), value);
	}

	public boolean hasData(PropData<?, ?> data) {
		return type.reverseData().containsKey(data);
	}

	public final boolean isTimeTraveling(long time) {
		return time <= createdTime - 20L || (lifespan > 0 && time > createdTime + lifespan + 20L);
	}

	public final boolean fullTick(long time) {
		if (isRemoved()) {
			return true;
		}

		snap();

		if (props.levelData.globalData.isReplay) {
			tick = Math.max(0, (int) (time - createdTime));
		}

		tick();

		if (lifespan > 0 && tick >= lifespan) {
			onExpired();
			remove(PropRemoveType.EXPIRED);
			return true;
		} else if (isRemoved()) {
			return true;
		}

		if (!paused && !props.levelData.globalData.isReplay) {
			tick++;
		}

		return false;
	}

	public final boolean fullTickBukkit(long time) {
		if (isRemoved()) {
			return true;
		}

		snap();
		tick();

		if (lifespan > 0 && tick >= lifespan) {
			onExpired();
			remove(PropRemoveType.EXPIRED);
			return true;
		} else if (isRemoved()) {
			return true;
		}

		if (!paused) {
			tick++;
		}

		return false;
	}

	public final void sync(PropData<?, ?> data) {
		var entry = type.reverseData().get(data);

		if (entry != null) {
			sync.add(entry);
		}
	}

	public final void sync(PropData<?, ?>... datas) {
		for (var data : datas) {
			sync(data);
		}
	}

	public final void syncAll() {
		for (var data : type.unsortedData()) {
			if (data != PAUSED) {
				sync(data);
			}
		}
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public final void setSize(double size) {
		setWidth(size);
		setHeight(size);
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

	public void moveTo(double x, double y, double z) {
		setPos(x, y, z);
	}

	public final void moveTo(Vector3dc pos) {
		moveTo(pos.x(), pos.y(), pos.z());
	}

	public final void moveTo(Position pos) {
		moveTo(pos.x(), pos.y(), pos.z());
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

	final byte[] getDataUpdates(Collection<PropDataEntry> syncSet) {
		return type.writeUpdate(level.registryAccess(), syncSet, this::getData);
	}

	public final byte[] getDataUpdates(boolean allData) {
		return getDataUpdates(allData ? type.data() : sync);
	}

	public final void update(RegistryAccess registryAccess, byte[] update, boolean allData) {
		type.readPropUpdate(this, registryAccess, update, allData, (k, v) -> setData(k, Cast.to(v)));
	}

	public final void remove(PropRemoveType removeType) {
		if (removed == PropRemoveType.NONE) {
			removed = removeType;
		}
	}

	public final void remove() {
		remove(PropRemoveType.GAME);
	}

	public final PropRemoveType getRemovedType() {
		return removed;
	}

	public final boolean isRemoved() {
		return removed != PropRemoveType.NONE;
	}

	public final boolean isClientSideOnly() {
		return clientSideOnly;
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

	public KNumberContext createNumberContext() {
		var ctx = props.levelData.createGlobalContext();
		ctx.sourcePos = new Vec3(pos.x, pos.y, pos.z);
		return ctx;
	}

	public double getDepth() {
		return width;
	}

	public void move() {
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
		for (var entry : type.data()) {
			var p = entry.data();
			var data = getData(p);
			nbt.put(p.key(), p.type().codec().encodeStart(ops, Cast.to(data)).getOrThrow());
		}
	}

	@Override
	public String toString() {
		return type.id() + "#" + getIdString();
	}

	public final String getIdString() {
		return "%08X".formatted(id);
	}

	public SimplePayload createAddPacket() {
		return new AddPropPayload(this);
	}

	public void handleAddPacket(Props<?> props) {
		props.add(this);
	}

	@Nullable
	public SimplePayload createUpdatePacket() {
		return UpdatePropPayload.of(this);
	}

	public double getMaxRenderDistance() {
		return 8192D;
	}

	public boolean isWithingRenderDistance(double squaredCenterDistanceToCamera) {
		var r = getMaxRenderDistance();
		return r >= Double.MAX_VALUE || squaredCenterDistanceToCamera <= r * r;
	}

	public boolean isVisibleInFrustum(double x, double y, double z, FrustumCheck frustum) {
		double w = width / 2D;
		double d = getDepth() / 2D;
		return frustum.isVisible(x - w, y, z - d, x + w, y + height, z + d);
	}

	public boolean isVisible(double x, double y, double z, FrustumCheck frustum, Vec3 cameraPos, double squaredCenterDistanceToCamera) {
		return isWithingRenderDistance(squaredCenterDistanceToCamera) && isVisibleInFrustum(x, y, z, frustum);
	}

	public void debugVisuals(Visuals visuals, double x, double y, double z, float delta, boolean selected) {
		if (selected) {
			visuals.add(new ColoredShape(new CuboidShape(Vec3f.of(width + 0.125D, height + 0.125D, getDepth() + 0.125D)).ref(), Gradient.EMPTY, Gradient.YELLOW).at(x, y + height / 2D, z));
		} else {
			visuals.add(new ColoredShape(new CuboidShape(Vec3f.of(width, height, getDepth())).ref(), Gradient.EMPTY, Gradient.WHITE).at(x, y + height / 2D, z));
		}
	}

	public Entity asEntity() {
		return new PropEntity(this);
	}

	public Vec3 getInfoPos(float delta) {
		return getPos(delta).add(0D, height * 1.1D, 0D);
	}

	public Component getDisplayName() {
		return Component.literal(type.translationKey());
	}

	public boolean shouldRenderDisplayName(Player to) {
		return false;
	}

	public boolean shouldRenderHealth(Player to) {
		return false;
	}

	public float getDisplayHealth(float delta) {
		return 1F;
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
		if (!level.isClientSide()) {
			var packet = type.createPayload(this, payload);

			if (packet != null) {
				PlayerTarget.of(level).s2c(packet);
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
				PlayerTarget.of(level).c2s(packet);
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

		double w = width / 2D;
		double d = getDepth() / 2D;

		if (collisionBox.intersects(pos.x - w, pos.y, pos.z - d, pos.x + w, pos.y + height, pos.z + d)) {
			if (level.isClientSide() && entity instanceof Player player) {
				onClientPlayerCollision(player);
			}

			return true;
		}

		return false;
	}

	public void addCollisionShapes(@Nullable Entity entity, List<VoxelShape> shapes) {
		double w = width / 2D;
		double d = getDepth() / 2D;

		shapes.add(Shapes.create(pos.x - w, pos.y, pos.z - d, pos.x + w, pos.y + height, pos.z + d));
	}

	public boolean canInteract(@Nullable Entity entity) {
		return true;
	}

	public List<AABB> getClipBoxes(@Nullable Entity entity) {
		double w = width / 2D;
		double d = getDepth() / 2D;

		return List.of(new AABB(pos.x - w, pos.y, pos.z - d, pos.x + w, pos.y + height, pos.z + d));
	}

	@Nullable
	public PropHitResult clip(ClipContext ctx) {
		var entity = EntityUtils.getEntity(ctx);

		if (entity != null && canInteract(entity)) {
			var hit = AABB.clip(getClipBoxes(entity), ctx.getFrom(), ctx.getTo(), BlockPos.ZERO);

			if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
				return new PropHitResult(this, hit.getLocation(), hit.getDirection(), BlockPos.containing(hit.getLocation()), false);
			}
		}

		return null;
	}

	public boolean onClientInteraction(Player player, int button, Vec3 at, Direction side) {
		if (button != 0 && button != 1 && button != 2) {
			return false;
		}

		PlayerTarget.of(level).c2s(new PropInteractionPayload(id, button, at, side));

		if (button == 0) {
			player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
		}

		return true;
	}

	public void onServerInteraction(ServerPlayer player, int button, Vec3 at, Direction side) {
		if (button == 0) {
			player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
		}
	}

	public void onClientPlayerCollision(Player player) {
		// Not needed in a Flashback context, because the server isn't real, and this event already happened in the recording.
		if (clientSideOnly || props.levelData.globalData.isReplay) {
			return;
		}

		var time = level.getGameTime();
		var last = lastPlayerCollisionTime;
		lastPlayerCollisionTime = time;

		if (last >= 0L && time - last <= 2L) {
			return;
		}

		double w = width / 2D;
		double d = getDepth() / 2D;
		var inside = player.getBoundingBox().intersects(pos.x - w, pos.y, pos.z - d, pos.x + w, pos.y + height, pos.z + d);
		PlayerTarget.of(player).c2s(new PropCollisionPayload(id, player.position(), inside));
	}

	public void onServerCollision(ServerPlayer player, Vec3 at, boolean inside) {
	}

	public void playSound(PositionedSoundData data, KNumberVariables variables) {
		VLFeatures.of(level).playGlobalSound(data, variables);
	}

	public void playSound(SoundData data, boolean looping, boolean stopImmediately) {
		playSound(new PositionedSoundData(data, this, looping, stopImmediately), KNumberVariables.EMPTY);
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

	public <T> void c2sEdit(PropData<?, T> data, T value, boolean sync) {
		if (!level.isClientSide()) {
			return;
		}

		setData(data, value);

		if (sync && !props.levelData.globalData.isReplay) {
			var payload = UpdatePropRequestPayload.of(this, List.of(data));

			if (payload != null) {
				PlayerTarget.of(level).c2s(payload);
			}
		}
	}

	public <O> O encode(DynamicOps<O> ops, O prefix) {
		for (var entry : type.data()) {
			try {
				var data = getData(entry.data());
				var result = entry.data().type().codec().encodeStart(ops, Cast.to(data));
				prefix = ops.set(prefix, entry.data().key(), result.getOrThrow());
			} catch (Exception ignore) {
			}
		}

		return prefix;
	}

	public <O> O encode(DynamicOps<O> ops) {
		return encode(ops, ops.empty());
	}

	public <O> DataResult<Prop> merge(DynamicOps<O> ops, O input) {
		var mapLike = ops.getMap(input).result().orElse(null);

		if (mapLike == null) {
			return DataResult.error(() -> "Expected a map input");
		}

		for (var entry : type.data()) {
			var p = entry.data();
			var t = mapLike.get(p.key());

			if (t != null) {
				var result = p.type().codec().parse(ops, t);

				if (result.isSuccess()) {
					setData(p, Cast.to(result.getOrThrow()));
					sync(p);
				} else {
					return DataResult.error(() -> "Failed to parse '" + p.key() + "' from " + t + ": " + result.error().get().message());
				}
			}
		}

		return DataResult.success(this);
	}

	public void setPausedAndSync(boolean paused) {
		if (this.paused != paused) {
			this.paused = paused;

			if (!level.isClientSide()) {
				PlayerTarget.of(level).s2c(new PausePropPayload(id, paused));
			}
		}
	}

	public DataResult<Prop> copy() {
		var ctx = props.context(type, PropSpawnType.GAME, level.getGameTime());
		var newProp = type.factory().create(ctx);

		for (var entry : type.data()) {
			var p = entry.data();
			newProp.setData(p, Cast.to(getData(p)));
		}

		return DataResult.success(newProp);
	}

	public int getPackedLight() {
		return BlockUtils.getPackedLight(level, getBlockPos());
	}

	public void getInterpolationData(Map<PropData<?, ?>, Object> map) {
		map.put(Prop.POSITION, new Vector3d(pos));

		if (type.contains(Prop.YAW)) {
			map.put(Prop.YAW, rotation.y);
		}

		if (type.contains(Prop.PITCH)) {
			map.put(Prop.PITCH, rotation.x);
		}

		if (type.contains(Prop.ROLL)) {
			map.put(Prop.ROLL, rotation.z);
		}
	}

	public void applyInterpolationData(PropInterpolationData data) {
		var pos = data.position(Prop.POSITION);

		if (pos != null) {
			setPos(pos);
		}

		var yaw = data.degrees(Prop.YAW);

		if (yaw != null) {
			setYaw(yaw.floatValue());
		}

		var pitch = data.degrees(Prop.PITCH);

		if (pitch != null) {
			setPitch(pitch.floatValue());
		}

		var roll = data.degrees(Prop.ROLL);

		if (roll != null) {
			setRoll(roll.floatValue());
		}
	}

	public double getCenterYOffset() {
		return height / 2D;
	}
}
