package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ListPlayersCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ListPlayersCommand.class)
public class ListPlayersCommandMixin {
	@ModifyExpressionValue(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;", ordinal = 0))
	private static LiteralArgumentBuilder<CommandSourceStack> vl$literal(LiteralArgumentBuilder<CommandSourceStack> original) {
		return original.requires(src -> CommonGameEngine.INSTANCE.allowPlayerListCommand(src));
	}
}
