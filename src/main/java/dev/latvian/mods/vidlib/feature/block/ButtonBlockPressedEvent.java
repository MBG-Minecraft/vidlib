package dev.latvian.mods.vidlib.feature.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.level.BlockEvent;

public class ButtonBlockPressedEvent extends BlockEvent {
	private final Level level;
	private final Player player;
	private final BlockHitResult hit;

	public ButtonBlockPressedEvent(Level level, BlockPos pos, BlockState state, Player player, BlockHitResult hit) {
		super(level, pos, state);
		this.level = level;
		this.player = player;
		this.hit = hit;
	}

	@Override
	public Level getLevel() {
		return level;
	}

	public Player getPlayer() {
		return player;
	}

	public BlockHitResult getHit() {
		return hit;
	}
}
