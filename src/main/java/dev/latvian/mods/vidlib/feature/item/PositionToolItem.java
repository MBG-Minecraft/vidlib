package dev.latvian.mods.vidlib.feature.item;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.klib.shape.ColoredShape;
import dev.latvian.mods.klib.shape.CubeShape;
import dev.latvian.mods.klib.shape.SphereShape;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.misc.ScreenText;
import dev.latvian.mods.vidlib.feature.misc.VidLibIcon;
import dev.latvian.mods.vidlib.feature.particle.ShapeParticleOptions;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public enum PositionToolItem implements VidLibTool, PlayerActionHandler {
	@AutoRegister
	INSTANCE;

	public enum Type {
		BLOCK("Block"),
		BLOCK_FACE("Block Face"),
		PLANE("Plane"),
		EYES("Eyes");

		public static final Type[] VALUES = values();

		public final String name;

		Type(String name) {
			this.name = name;
		}

		@Nullable
		public Vec3 position(Player player, @Nullable BlockHitResult hit) {
			return switch (this) {
				case BLOCK -> hit == null ? null : Vec3.atCenterOf(hit.getBlockPos());
				case BLOCK_FACE -> hit == null ? null : Vec3.atCenterOf(hit.getBlockPos().relative(hit.getDirection()));
				case PLANE -> hit == null ? null : hit.getLocation();
				case EYES -> player.getEyePosition();
			};
		}
	}

	public Vec3 lastClick = null;
	public Type clientMode = Type.BLOCK;
	public Vec3 clientPos = null;

	@Override
	public String getId() {
		return "pos";
	}

	@Override
	public Component getName() {
		return Component.literal("Position Tool");
	}

	@Override
	public ResourceLocation getModel() {
		return ResourceLocation.withDefaultNamespace("breeze_rod");
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (player.level().isClientSide()) {
			if (clientPos == null) {
				return true;
			}

			if (lastClick == null) {
				lastClick = clientPos;
			}

			var blockPos = BlockPos.containing(clientPos);
			var strBlock = KMath.format(blockPos);
			var strVec = KMath.format(clientPos);
			var strDist = KMath.format(lastClick.distanceTo(clientPos));

			lastClick = clientPos;

			player.tell(Component.empty()
				.append(Component.empty()
					.withStyle(Style.EMPTY.withCopyString(strBlock))
					.append(VidLibIcon.COPY.prefix())
					.append(Component.literal(strBlock).withStyle(Style.EMPTY.withHoverText("Block Position")))
				)
				.append(" | ")
				.append(Component.empty()
					.withStyle(Style.EMPTY.withCopyString(strVec))
					.append(VidLibIcon.COPY.prefix())
					.append(Component.literal(strVec).withStyle(Style.EMPTY.withHoverText("Vec3 Position")))
				)
				.append(" | ")
				.append(Component.empty()
					.withStyle(Style.EMPTY.withCopyString(strDist))
					.append(VidLibIcon.COPY.prefix())
					.append(Component.literal(strDist).withStyle(Style.EMPTY.withHoverText("Distance from last click")))
				)
			);

			player.level().addParticle(new ShapeParticleOptions(60, Color.CYAN, Color.WHITE), blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 0D, 0D, 0D);
		}

		return true;
	}

	@Override
	public boolean useOnEntity(Player player, ItemStack item, Entity target) {
		if (player.level().isClientSide()) {
			var str = KMath.format(target.position());
			player.tell(Component.empty().append(target.getName()).append(": " + str).withStyle(Style.EMPTY.withCopyString(str)));
		}

		return true;
	}

	@Override
	public void renderSetup(Player player, ItemStack item, @Nullable HitResult hit, float delta) {
		var tag = item.get(DataComponents.CUSTOM_DATA).getUnsafe();
		clientMode = Type.VALUES[tag.getByteOr("position_tool_mode", (byte) 0)];
		var chit = player.ray(500D, delta).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);
		clientPos = chit instanceof BlockHitResult blockHit ? clientMode.position(player, blockHit) : clientMode.position(player, null);
	}

	@Override
	public void debugText(Player player, ItemStack item, @Nullable HitResult hit, ScreenText screenText) {
		var tag = item.get(DataComponents.CUSTOM_DATA).getUnsafe();
		var mode = Type.VALUES[tag.getByteOr("position_tool_mode", (byte) 0)];
		screenText.topRight.add("Mode: " + mode.name);

		if (clientPos == null) {
			return;
		}

		var blockPos = BlockPos.containing(clientPos);
		screenText.topRight.add(player.level().getBlockState(blockPos).getBlock().getName());
		screenText.topRight.add(KMath.format(blockPos));
		screenText.topRight.add(KMath.format(clientPos));

		if (lastClick != null) {
			screenText.topRight.add(KMath.format(lastClick.distanceTo(clientPos)) + " m");
		}
	}

	@Override
	public void visuals(Player player, ItemStack item, Visuals visuals, float delta) {
		if (clientPos != null) {
			if (clientMode == Type.PLANE) {
				visuals.addLine(new Line(clientPos, new Vec3(clientPos.x + 0.5D, clientPos.y, clientPos.z)), Color.WHITE, Color.WHITE.withAlpha(0));
				visuals.addLine(new Line(clientPos, new Vec3(clientPos.x, clientPos.y + 0.5D, clientPos.z)), Color.WHITE, Color.WHITE.withAlpha(0));
				visuals.addLine(new Line(clientPos, new Vec3(clientPos.x, clientPos.y, clientPos.z + 0.5D)), Color.WHITE, Color.WHITE.withAlpha(0));
				visuals.add(new ColoredShape(SphereShape.UNIT, Color.CYAN, Color.TRANSPARENT).at(clientPos));
				visuals.addOutline(new ColoredShape(SphereShape.UNIT, Color.WHITE, Color.TRANSPARENT).at(clientPos));
			} else {
				visuals.add(new ColoredShape(CubeShape.UNIT, Color.CYAN, Color.WHITE).at(clientPos));
			}
		}
	}

	@Override
	public Set<PlayerActionType> getHandledPlayerActions() {
		return PlayerActionType.SWAP_SET;
	}

	@Override
	public void onPlayerAction(ServerPlayer player, PlayerActionType action) {
		if (action == PlayerActionType.SWAP) {
			var item = player.getMainHandItem();
			var tag = item.get(DataComponents.CUSTOM_DATA).copyTag();
			var mode = Type.VALUES[(tag.getByteOr("position_tool_mode", (byte) 0) + 1) % Type.VALUES.length];
			tag.putByte("position_tool_mode", (byte) mode.ordinal());
			item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}
}
