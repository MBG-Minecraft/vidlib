package dev.beast.mods.shimmer.feature.item;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.misc.ScreenText;
import dev.beast.mods.shimmer.feature.misc.ShimmerIcon;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.latvian.mods.kmath.KMath;
import dev.latvian.mods.kmath.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PositionToolItem implements ShimmerTool {
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

	public static Vec3 lastClick = null;

	@AutoInit
	public static void bootstrap() {
		ShimmerTool.register(new PositionToolItem());
	}

	@Override
	public String getId() {
		return "pos";
	}

	@Override
	public Component getName() {
		return Component.literal("Position Tool");
	}

	@Override
	public ItemStack createItem() {
		return new ItemStack(Items.BREEZE_ROD);
	}

	@Override
	public boolean rightClick(Player player, ItemStack item, @Nullable BlockHitResult hit) {
		if (player.shimmer$sessionData().input.alt()) {
			if (!player.level().isClientSide()) {
				var tag = item.get(DataComponents.CUSTOM_DATA).copyTag();
				var mode = Type.VALUES[(tag.getByte("position_tool_mode") + 1) % Type.VALUES.length];
				tag.putByte("position_tool_mode", (byte) mode.ordinal());
				item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			}

			return true;
		}

		if (player.level().isClientSide()) {
			var tag = item.get(DataComponents.CUSTOM_DATA).getUnsafe();
			var mode = Type.VALUES[tag.getByte("position_tool_mode")];
			var pos = mode.position(player, hit);

			if (pos == null) {
				return true;
			}

			if (lastClick == null) {
				lastClick = pos;
			}

			var blockPos = BlockPos.containing(pos);
			var strBlock = KMath.formatBlockPos(blockPos);
			var strVec = KMath.formatVec3(pos);
			var strDist = KMath.format(lastClick.distanceTo(pos));

			lastClick = pos;

			player.tell(Component.empty()
				.append(Component.empty()
					.withStyle(Style.EMPTY.withCopyString(strBlock))
					.append(ShimmerIcon.COPY.prefix())
					.append(Component.literal(strBlock).withStyle(Style.EMPTY.withHoverText("Block Position")))
				)
				.append(" | ")
				.append(Component.empty()
					.withStyle(Style.EMPTY.withCopyString(strVec))
					.append(ShimmerIcon.COPY.prefix())
					.append(Component.literal(strVec).withStyle(Style.EMPTY.withHoverText("Vec3 Position")))
				)
				.append(" | ")
				.append(Component.empty()
					.withStyle(Style.EMPTY.withCopyString(strDist))
					.append(ShimmerIcon.COPY.prefix())
					.append(Component.literal(strDist).withStyle(Style.EMPTY.withHoverText("Distance from last click")))
				)
			);

			player.level().addParticle(new CubeParticleOptions(Color.CYAN, Color.WHITE, 60), blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 0D, 0D, 0D);
		}

		return true;
	}

	@Override
	public boolean useOnEntity(Player player, ItemStack item, Entity target) {
		if (player.level().isClientSide()) {
			var str = KMath.formatVec3(target.position());
			player.tell(Component.empty().append(target.getName()).append(": " + str).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to Copy")))));
		}

		return true;
	}

	@Override
	public void debugText(Player player, ItemStack item, @Nullable HitResult hit, ScreenText screenText) {
		var tag = item.get(DataComponents.CUSTOM_DATA).getUnsafe();
		var mode = Type.VALUES[tag.getByte("position_tool_mode")];
		screenText.topRight.add("Mode: " + mode.name);

		if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
			hit = player.ray(500D, 1F).hitBlock(player, ClipContext.Fluid.SOURCE_ONLY);
		}

		var pos = hit instanceof BlockHitResult blockHit ? mode.position(player, blockHit) : mode.position(player, null);

		if (pos == null) {
			return;
		}

		var blockPos = BlockPos.containing(pos);
		player.level().cubeParticles(new CubeParticleOptions(Color.CYAN, Color.WHITE, 1), List.of(blockPos));

		screenText.topRight.add(player.level().getBlockState(blockPos).getBlock().getName());
		screenText.topRight.add(KMath.formatBlockPos(blockPos));
		screenText.topRight.add(KMath.formatVec3(pos));

		if (lastClick != null) {
			screenText.topRight.add(KMath.format(lastClick.distanceTo(pos)) + " m");
		}
	}
}
