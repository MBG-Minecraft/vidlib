package dev.latvian.mods.vidlib.feature.platform.neoforge;

import dev.latvian.mods.vidlib.core.VLServerPacketListener;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import dev.latvian.mods.vidlib.feature.session.LoginData;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NeoForgePacketContext(VidLibPacketPayloadContainer payload, IPayloadContext parent) implements Context {
	@Override
	public ICommonPacketListener listener() {
		return parent.listener();
	}

	@Override
	public Player player() {
		return parent.player();
	}

	@Override
	public void finishTask(ConfigurationTask.Type type) {
		parent.finishCurrentTask(type);
	}

	@Override
	public void addLoginData(LoginData data) {
		if (listener() instanceof VLServerPacketListener) {
			// WIP
		}
	}
}
