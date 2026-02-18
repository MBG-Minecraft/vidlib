package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TeamMsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
	@ModifyExpressionValue(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;"))
	private static LiteralArgumentBuilder<CommandSourceStack> video$literal(LiteralArgumentBuilder<CommandSourceStack> original) {
		return original.requires(src -> CommonGameEngine.INSTANCE.allowTeamMsgCommand(src));
	}
}
