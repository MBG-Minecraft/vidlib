package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.core.api.client.VLJoinMultiplayerScreen;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.client.OtherServerHeader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin extends ObjectSelectionList<ServerSelectionList.Entry> {
	@Shadow
	@Final
	private JoinMultiplayerScreen screen;

	@Shadow
	@Final
	private List<ServerSelectionList.OnlineServerEntry> onlineServers;

	@Unique
	private final List<ServerSelectionList.OnlineServerEntry> vl$hubServers = new ArrayList<>();

	public ServerSelectionListMixin(Minecraft mc, int width, int height, int y, int itemHeight) {
		super(mc, width, height, y, itemHeight);
	}

	@Inject(method = "refreshEntries", at = @At("RETURN"))
	private void vl$refreshEntries(CallbackInfo ci) {
		if (vl$hubServers.isEmpty()) {
			return;
		}

		if (!onlineServers.isEmpty()) {
			addEntryToTop(new OtherServerHeader());
		}

		for (int i = vl$hubServers.size() - 1; i >= 0; i--) {
			addEntryToTop(vl$hubServers.get(i));
		}
	}

	@Inject(method = "updateOnlineServers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;refreshEntries()V"))
	private void vl$updateOnlineServers(ServerList servers, CallbackInfo ci) {
		var selectionList = (ServerSelectionList) (Object) this;
		var hubServers = ((VLJoinMultiplayerScreen) screen).vl$hubServers();
		hubServers.clear();

		var list = HubClientSession.CURRENT.servers;
		vl$hubServers.clear();

		for (int i = 0; i < list.size(); i++) {
			var d = list.get(i);
			var serverData = new ServerData(d.name(), d.location(), ServerData.Type.OTHER);
			d.icon().ifPresent(serverData::setIconBytes);
			var entry = selectionList.new OnlineServerEntry(screen, serverData);
			vl$hubServers.add(i, entry);
			hubServers.add(entry);
		}
	}
}
