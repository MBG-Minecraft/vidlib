package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLJoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin extends ScreenMixin implements VLJoinMultiplayerScreen {
	@Shadow
	protected ServerSelectionList serverSelectionList;

	@Shadow
	private ServerList servers;

	@Shadow
	private Button editButton;
	@Shadow
	private Button deleteButton;
	@Unique
	private final Collection<ServerSelectionList.Entry> vl$hubServers = new HashSet<>();

	@Override
	public void vl$refresh() {
		this.serverSelectionList.updateOnlineServers(this.servers);
	}

	@Override
	public Collection<ServerSelectionList.Entry> vl$hubServers() {
		return vl$hubServers;
	}

	@Inject(method = "onSelectedChange", at = @At("RETURN"))
	private void vl$onSelectedChange(CallbackInfo ci) {
		var selected = this.serverSelectionList.getSelected();

		if (selected != null && vl$hubServers().contains(selected)) {
			this.editButton.active = false;
			this.deleteButton.active = false;
		}
	}
}
