package dev.latvian.mods.vidlib.feature.prop;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
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
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.AngleImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.IntImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Vector3dImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfField;
import dev.latvian.mods.vidlib.feature.screeneffect.dof.DepthOfFieldPanel;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.SoundData;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.math.knumber.KNumberContext;
import dev.latvian.mods.vidlib.math.knumber.KNumberVariables;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.PositionType;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import it.unimi.dsi.fastutil.Pair;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Prop {
	public static final PropData<Prop, Integer> TICK = PropData.create(Prop.class, "tick", DataTypes.TICKS, p -> p.tick, (p, v) -> p.tick = v, IntImBuilder.TYPE_1M);
	public static final PropData<Prop, Integer> LIFESPAN = PropData.create(Prop.class, "lifespan", DataTypes.TICKS, p -> p.lifespan, (p, v) -> p.lifespan = v, IntImBuilder.TYPE_1M);
	public static final PropData<Prop, Vector3d> POSITION = PropData.create(Prop.class, "position", JOMLDataTypes.DVEC3, p -> p.pos, Prop::setPos, Vector3dImBuilder.TYPE);
	public static final PropData<Prop, Float> PITCH = PropData.create(Prop.class, "pitch", DataTypes.FLOAT, p -> p.rotation.x, Prop::setPitch, AngleImBuilder.TYPE_90);
	public static final PropData<Prop, Float> YAW = PropData.create(Prop.class, "yaw", DataTypes.FLOAT, p -> p.rotation.y, Prop::setYaw, AngleImBuilder.TYPE_180);
	public static final PropData<Prop, Float> ROLL = PropData.create(Prop.class, "roll", DataTypes.FLOAT, p -> p.rotation.z, Prop::setRoll, AngleImBuilder.TYPE_180);
	public static final PropData<Prop, Float> WIDTH = PropData.create(Prop.class, "width", DataTypes.FLOAT, p -> (float) p.width, (p, v) -> p.width = v, FloatImBuilder.type(0F, 16F));
	public static final PropData<Prop, Float> HEIGHT = PropData.create(Prop.class, "height", DataTypes.FLOAT, p -> (float) p.height, (p, v) -> p.height = v, FloatImBuilder.type(0F, 16F));
	public static final PropData<Prop, Boolean> CAN_COLLIDE = PropData.createBoolean(Prop.class, "can_collide", p -> p.canCollide, (p, v) -> p.canCollide = v);
	public static final PropData<Prop, Boolean> CAN_INTERACT = PropData.createBoolean(Prop.class, "can_interact", p -> p.canInteract, (p, v) -> p.canInteract = v);
	public static final PropData<Prop, Boolean> PAUSED = PropData.createBoolean(Prop.class, "paused", p -> p.paused, (p, v) -> p.paused = v);

	public final PropType<?> type;
	public final PropSpawnType spawnType;
	public final long createdTime;
	final Set<PropDataEntry> sync;
	public Level level;
	public int id;
	PropRemoveType removed;
	private List<Pair<PropData<?, ?>, ImBuilder<?>>> imguiBuilders;
	Map<PropData<?, ?>, Object> defaultValues = Map.of();
	boolean clientSideOnly;

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

		if (level.isReplayLevel()) {
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

		if (!paused && !level.isReplayLevel()) {
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

	final byte[] getDataUpdates(boolean allData) {
		return getDataUpdates(allData ? type.data() : sync);
	}

	final void update(RegistryAccess registryAccess, byte[] update, boolean allData) {
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

	public KNumberContext createWorldNumberContext() {
		var ctx = level.getGlobalContext();
		ctx.sourcePos = new Vec3(pos.x, pos.y, pos.z);
		return ctx;
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

	public SimplePacketPayload createAddPacket() {
		return new AddPropPayload(this);
	}

	public void handleAddPacket(Props<?> props) {
		props.add(this);
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

	public void debugVisuals(Visuals visuals, double x, double y, double z, float delta, boolean selected) {
		if (selected) {
			visuals.add(new ColoredShape(new CuboidShape(Vec3f.of(width + 0.125D, height + 0.125D, width + 0.125D), Rotation.NONE), Color.TRANSPARENT, Color.YELLOW).at(x, y + height / 2D, z));
		} else {
			visuals.add(new ColoredShape(new CuboidShape(Vec3f.of(width, height, width), Rotation.NONE), Color.TRANSPARENT, Color.WHITE).at(x, y + height / 2D, z));
		}
	}

	public float getDebugVisualsProgress(float delta) {
		// return getRelativeTick(delta, 1F);
		return -1F;
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
	public PropHitResult clip(ClipContext ctx) {
		var entity = ctx.getEntity();

		if (canInteract(entity)) {
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

		level.c2s(new PropInteractionPayload(spawnType.listType, id, button, at, side));

		if (button == 0) {
			player.swing(InteractionHand.MAIN_HAND, true);
		}

		return true;
	}

	public void onServerInteraction(ServerPlayer player, int button, Vec3 at, Direction side) {
		if (button == 0) {
			player.swing(InteractionHand.MAIN_HAND, false);
		}
	}

	public void imgui(ImGraphics graphics, float delta) {
		graphics.pushStack();

		if (graphics.isReplay && !clientSideOnly) {
			ImGui.beginDisabled();
		}

		graphics.setRedButton();

		if (ImGui.button(ImIcons.TRASHCAN + "###remove")) {
			graphics.mc.runClientCommand((clientSideOnly ? "client-prop remove id " : "prop remove id ") + getIdString());
		}

		ImGuiUtils.hoveredTooltip("Remove");

		if (graphics.isReplay && !clientSideOnly) {
			ImGui.endDisabled();
		}

		graphics.popStack();

		ImGui.sameLine();

		boolean isHidden = ClientProps.HIDDEN_PROPS.contains(id);

		if (graphics.button((isHidden ? ImIcons.INVISIBLE : ImIcons.VISIBLE) + "###visible", isHidden ? ImColorVariant.RED : null)) {
			if (isHidden) {
				ClientProps.HIDDEN_PROPS.remove(id);
			} else {
				ClientProps.HIDDEN_PROPS.add(id);
			}
		}

		ImGuiUtils.hoveredTooltip(isHidden ? "Hidden" : "Visible");

		ImGui.sameLine();

		boolean isTypeHidden = ClientProps.HIDDEN_PROP_TYPES.contains(type);

		if (graphics.button((isTypeHidden ? ImIcons.INVISIBLE : ImIcons.VISIBLE) + ImIcons.ASTERIX.toString() + "###type-visible", isTypeHidden ? ImColorVariant.RED : null)) {
			if (isTypeHidden) {
				ClientProps.HIDDEN_PROP_TYPES.remove(type);
			} else {
				ClientProps.HIDDEN_PROP_TYPES.add(type);
			}
		}

		ImGuiUtils.hoveredTooltip(isTypeHidden ? "Type Hidden" : "Type Visible");

		ImGui.sameLine();

		if (RecordedProp.LIST != null && !clientSideOnly) {
			ImGui.beginDisabled();
		}

		if (ImGui.button(ImIcons.PASTE + "###clone")) {
			graphics.mc.runClientCommand((clientSideOnly ? "client-prop clone " : "prop clone ") + getIdString());
		}

		ImGuiUtils.hoveredTooltip("Clone");

		if (RecordedProp.LIST != null && !clientSideOnly) {
			ImGui.endDisabled();
		}

		if (graphics.isReplay) {
			if (DepthOfField.OVERRIDE_ENABLED.get()) {
				ImGui.sameLine();

				if (ImGui.button(ImIcons.APERTURE + "###focus-dof")) {
					DepthOfField.OVERRIDE = DepthOfField.OVERRIDE.withFocus(KVector.following(this, PositionType.EYES));
					DepthOfFieldPanel.INSTANCE.builder.set(DepthOfField.OVERRIDE);
				}

				ImGuiUtils.hoveredTooltip("Focus DoF");
			}
		}

		ImGui.sameLine();

		if (ImGui.button(ImIcons.COPY + "###copy-id")) {
			ImGui.setClipboardText(getIdString());
		}

		ImGuiUtils.hoveredTooltip("Copy ID");

		if (clientSideOnly) {
			ImGui.sameLine();
			graphics.button(ImIcons.WARNING + "###client-only", ImColorVariant.ORANGE);
			ImGuiUtils.hoveredTooltip("Client-Side Only!");
		}

		if (imguiBuilders == null) {
			imguiBuilders = new ArrayList<>(type.data().size());

			for (var data : type.unsortedData()) {
				if (data == TICK || data == LIFESPAN || data == PAUSED) {
					continue;
				}

				if (data.imBuilder() != null) {
					imguiBuilders.add(Pair.of(data, data.imBuilder().get()));
				}
			}

			imguiBuilders = List.copyOf(imguiBuilders);
		}

		if (ImGui.beginTable("###data", 3, ImGuiTableFlags.SizingStretchProp | ImGuiTableFlags.Borders)) {
			if (hasData(TICK)) {
				ImGui.tableNextRow();
				ImGui.pushID("tick");
				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				if (ImGui.smallButton(ImIcons.UNDO + "###reset")) {
					prevTick = 0;
					tick = 0;
					c2sEdit(TICK, 0, true);
				}

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				if (ImGui.checkbox("tick", !paused)) {
					if (paused) {
						graphics.mc.runClientCommand((clientSideOnly ? "client-prop unpause " : "prop unpause ") + getIdString());
					} else {
						graphics.mc.runClientCommand((clientSideOnly ? "client-prop pause " : "prop pause ") + getIdString());
					}
				}

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				int lifespan1 = lifespan > 0 && hasData(LIFESPAN) ? lifespan : 0;
				var tickText = lifespan1 > 0 ? "%,d / %,d".formatted(tick, lifespan1) : "%,d".formatted(tick);

				if (paused) {
					ImGuiUtils.INT.set(tick);
					ImGui.pushItemWidth(-1F);
					ImGui.dragInt("###value", ImGuiUtils.INT.getData(), 1F, 0F, 1000000F, tickText);
					ImGui.popItemWidth();

					var update = ImUpdate.itemEdit();

					if (update.isAny()) {
						c2sEdit(TICK, Math.max(0, ImGuiUtils.INT.get()), update.isFull());
					}
				} else {
					ImGui.text(tickText);
				}

				ImGui.popID();

				if (lifespan1 > 0) {
					ImGui.tableNextRow();
					ImGui.pushID("progress");
					ImGui.tableNextColumn();
					ImGui.beginDisabled();
					ImGui.alignTextToFramePadding();
					ImGui.smallButton(ImIcons.UNDO + "###reset");
					ImGui.endDisabled();
					ImGui.tableNextColumn();
					ImGui.alignTextToFramePadding();
					ImGui.text("progress");
					ImGui.tableNextColumn();
					ImGui.progressBar(getRelativeTick(delta, 0F), 0F, 20F);
					ImGui.popID();
				}
			}

			for (var entry : imguiBuilders) {
				var data = entry.left();
				var builder = entry.right();

				ImGui.tableNextRow();
				ImGui.pushID(data.key());
				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();

				var value = getData(data);

				try {
					builder.set(Cast.to(value));
				} catch (Throwable ex) {
					graphics.stackTrace(ex);
				}

				var defaultValue = defaultValues.get(data);
				var isDefault = Objects.equals(defaultValue, value);

				if (isDefault) {
					ImGui.beginDisabled();
				}

				if (ImGui.smallButton(ImIcons.UNDO + "###reset")) {
					c2sEdit(data, Cast.to(defaultValue), true);
				}

				if (!isDefault && ImGui.isItemHovered()) {
					ImGui.setTooltip("Reset to " + defaultValue);
				}

				if (isDefault) {
					ImGui.endDisabled();
				}

				ImGui.tableNextColumn();
				ImGui.alignTextToFramePadding();
				graphics.redTextIf(data.key(), !builder.isValid());

				ImGui.tableNextColumn();
				ImGui.pushItemWidth(-1F);
				ImGui.pushID("###value");
				var update = builder.imgui(graphics);
				ImGui.popID();
				ImGui.popItemWidth();

				if (update.isAny() && builder.isValid()) {
					c2sEdit(data, Cast.to(builder.build()), update.isFull());
				}

				ImGui.popID();
			}

			ImGui.endTable();
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

	public <T> void c2sEdit(PropData<?, T> data, T value, boolean sync) {
		if (!level.isClientSide) {
			return;
		}

		setData(data, value);

		if (sync && !level.isReplayLevel()) {
			var payload = UpdatePropRequestPayload.of(this, List.of(data));

			if (payload != null) {
				level.c2s(payload);
			}
		}
	}

	public JsonObject getDataJson(DynamicOps<JsonElement> ops) {
		var json = new JsonObject();

		for (var entry : type.data()) {
			try {
				json.add(entry.data().key(), entry.data().type().codec().encodeStart(ops, Cast.to(getData(entry.data()))).getOrThrow());
			} catch (Exception ignore) {
			}
		}

		return json;
	}

	public void setDataJson(DynamicOps<JsonElement> ops, JsonObject json) {
		for (var entry : type.data()) {
			var p = entry.data();
			var t = json.get(p.key());

			if (t != null) {
				var result = p.type().codec().parse(ops, t);

				if (result.isSuccess()) {
					setData(p, Cast.to(result.getOrThrow()));
				}
			}
		}
	}

	public void setPausedAndSync(boolean paused) {
		if (this.paused != paused) {
			this.paused = paused;

			if (!level.isClientSide()) {
				level.s2c(new PausePropPayload(spawnType.listType, id, paused));
			}
		}
	}

	public DataResult<Prop> copy() {
		var ctx = level.getProps().context(type, PropSpawnType.GAME, level.getGameTime());
		var newProp = type.factory().create(ctx);

		for (var entry : type.data()) {
			var p = entry.data();
			newProp.setData(p, Cast.to(getData(p)));
		}

		return DataResult.success(newProp);
	}
}
