package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLCommandSourceStack;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin implements VLCommandSourceStack {
	@Shadow
	@Final
	private MinecraftServer server;

	@Shadow
	@Final
	public CommandSource source;

	@Shadow
	public abstract Component getDisplayName();

	/**
	 * @author Lat
	 * @reason MBG
	 */
	@Overwrite
	private void broadcastToAdmins(Component message) {
		var component = Component.translatable("chat.type.admin", getDisplayName(), message).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

		if (server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
			for (var player : server.getPlayerList().getPlayers()) {
				if (player.commandSource() != source && CommonGameEngine.INSTANCE.getReceiveCommandFeedback(server, player)) {
					player.sendSystemMessage(component);
				}
			}
		}

		if (source != server && server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
			server.sendSystemMessage(component);
		}
	}
}
