package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLJoinMultiplayerScreen;
import dev.mrbeastgaming.mods.hub.api.HubGameServerData;
import dev.mrbeastgaming.mods.hub.client.OtherServerHeader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.objectweb.asm.Opcodes;
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

	@Inject(method = "refreshEntries", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;onlineServers:Ljava/util/List;", opcode = Opcodes.GETFIELD))
	private void vl$refreshEntries(CallbackInfo ci) {
		vl$hubServers.forEach(this::addEntry);

		if (!vl$hubServers.isEmpty() && !onlineServers.isEmpty()) {
			addEntry(new OtherServerHeader());
		}
	}

	@Inject(method = "updateOnlineServers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;refreshEntries()V"))
	private void vl$updateOnlineServers(ServerList servers, CallbackInfo ci) {
		var selectionList = (ServerSelectionList) (Object) this;
		var hubServers = ((VLJoinMultiplayerScreen) screen).vl$hubServers();
		hubServers.clear();

		var list = HubGameServerData.CURRENT;
		vl$hubServers.clear();

		for (int i = 0; i < list.size(); i++) {
			var d = list.get(i);
			var serverData = new ServerData(d.name(), d.location(), ServerData.Type.OTHER);
			serverData.setIconBytes(d.icon().orElse(null));
			var entry = selectionList.new OnlineServerEntry(screen, serverData);
			vl$hubServers.add(i, entry);
			hubServers.add(entry);
		}
	}
}
