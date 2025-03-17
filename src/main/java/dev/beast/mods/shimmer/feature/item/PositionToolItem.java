package dev.beast.mods.shimmer.feature.item;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.misc.DebugText;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

public class PositionToolItem implements ShimmerTool {
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
	public boolean useOnBlock(Player player, ItemStack item, UseItemOnBlockEvent event) {
		if (event.getLevel().isClientSide()) {
			var pos = event.getPos();
			var str = player.isShiftKeyDown() ? KMath.formatVec3(pos.getCenter()) : KMath.formatBlockPos(pos);
			var state = event.getLevel().getBlockState(event.getPos());
			player.tell(Component.empty().append(state.getBlock().getName()).append(": " + str).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to Copy")))));
			event.getLevel().addParticle(new CubeParticleOptions(Color.CYAN, Color.WHITE, 60), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
		}

		return true;
	}

	@Override
	public boolean use(Player player, ItemStack item) {
		if (player.level().isClientSide()) {
			var pos = BlockPos.containing(player.getEyePosition());
			var str = player.isShiftKeyDown() ? KMath.formatVec3(pos.getCenter()) : KMath.formatBlockPos(pos);
			player.tell(Component.literal("Air: " + str).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to Copy")))));
			player.level().addParticle(new CubeParticleOptions(Color.CYAN, Color.WHITE, 60), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
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
	public void debugText(Player player, ItemStack item, @Nullable HitResult hit, DebugText debugText) {
		if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhit) {
			var pos = bhit.getBlockPos();
			var str = player.isShiftKeyDown() ? KMath.formatVec3(pos.getCenter()) : KMath.formatBlockPos(pos);
			debugText.topRight.add(Component.literal(str));
		} else if (hit instanceof EntityHitResult ehit) {
			debugText.topRight.add(Component.literal(KMath.formatVec3(ehit.getEntity().position())));
		}
	}
}
