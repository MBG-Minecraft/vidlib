package dev.latvian.mods.vidlib.core.mixin.neo;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(ClientCommandSourceStack.class)
public abstract class ClientCommandSourceStackMixin extends CommandSourceStack {
	public ClientCommandSourceStackMixin(CommandSource source, Vec3 worldPosition, Vec2 rotation, ServerLevel level, int permissionLevel, String textName, Component displayName, MinecraftServer server, @Nullable Entity entity) {
		super(source, worldPosition, rotation, level, permissionLevel, textName, displayName, server, entity);
	}

	@Override
	public Level getSidedLevel() {
		return Minecraft.getInstance().level;
	}

	@Inject(method = "getOnlinePlayerNames", at = @At("HEAD"), cancellable = true)
	private void vl$getOnlinePlayerNames(CallbackInfoReturnable<Collection<String>> cir) {
		var override = ClientGameEngine.INSTANCE.overrideCommandOnlinePlayerNames();

		if (override != null) {
			cir.setReturnValue(override);
		}
	}
}
