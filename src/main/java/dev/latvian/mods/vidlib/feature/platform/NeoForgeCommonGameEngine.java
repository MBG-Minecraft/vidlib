package dev.latvian.mods.vidlib.feature.platform;

import de.maxhenkel.voicechat.Voicechat;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.integration.voicechat.VoiceChatIntegration;
import dev.mrbeastgaming.mods.hub.api.UsedPort;
import dev.mrbeastgaming.mods.hub.api.gateway.UsedPortsEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NeoForgeCommonGameEngine extends CommonGameEngine {
	@Override
	@Nullable
	public VoxelShape overrideBarrierShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext ctx && ctx.getEntity() != null && blockGetter instanceof Level level && level.getServerFeatures().has(Feature.SOFT_BARRIERS)) {
			if (ctx.getEntity() instanceof AbstractArrow) {
				return Shapes.empty();
			}

			if (ctx.getEntity().vl$isCreative()) {
				return Shapes.empty();
			}
		}

		return null;
	}

	@Override
	public boolean hasImprovedPlayerTags() {
		return true;
	}

	@Override
	public boolean allowFlight(Player player) {
		return player.get(InternalPlayerData.CAN_FLY);
	}

	@Override
	public void getUsedPorts(MinecraftServer server, List<UsedPort> list) {
		super.getUsedPorts(server, list);
		NeoForge.EVENT_BUS.post(new UsedPortsEvent(list));

		if (PlatformHelper.CURRENT.isModLoaded(Voicechat.MODID)) {
			voiceChatIntegration(list);
		}
	}

	private void voiceChatIntegration(List<UsedPort> list) {
		VoiceChatIntegration.addUsedPorts(list);
	}
}
