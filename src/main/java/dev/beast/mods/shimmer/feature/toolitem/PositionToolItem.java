package dev.beast.mods.shimmer.feature.toolitem;

import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PositionToolItem implements ToolItem {
	@Override
	public Component getName() {
		return Component.literal("Position Tool");
	}

	@Override
	public boolean useOnBlock(Player player, UseItemOnBlockEvent event) {
		if (event.getLevel().isClientSide()) {
			var pos = event.getPos();
			var str = player.isShiftKeyDown() ? KMath.formatVec3(pos.getCenter()) : KMath.formatBlockPos(pos);
			player.tell(Component.literal(str).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to Copy")))));
			event.getLevel().addParticle(new CubeParticleOptions(Color.CYAN, Color.WHITE, 60), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
		}

		return true;
	}

	@Override
	public boolean use(Player player, PlayerInteractEvent.RightClickItem event) {
		if (player.level().isClientSide()) {
			var pos = BlockPos.containing(player.getEyePosition());
			var str = player.isShiftKeyDown() ? KMath.formatVec3(pos.getCenter()) : KMath.formatBlockPos(pos);
			player.tell(Component.literal(str).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to Copy")))));
			event.getLevel().addParticle(new CubeParticleOptions(Color.CYAN, Color.WHITE, 60), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
		}

		return true;
	}

	@Override
	public void addDebugText(ItemStack item, Player player, @Nullable HitResult hit, List<Component> left, List<Component> right) {
		if (hit != null && hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhit) {
			var pos = bhit.getBlockPos();
			right.add(Component.literal("%d, %d, %d".formatted(pos.getX(), pos.getY(), pos.getZ())));
		}
	}
}
