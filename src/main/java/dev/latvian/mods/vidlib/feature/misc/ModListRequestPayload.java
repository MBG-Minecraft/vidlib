package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.net.ConfigurationTaskHolder;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VLConfigurationTask;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.fml.ModList;

import java.util.ArrayList;

public enum ModListRequestPayload implements SimplePacketPayload {
	INSTANCE;

	@AutoPacket(stage = AutoPacket.Stage.CONFIG)
	public static final VidLibPacketType<ModListRequestPayload> TYPE = VidLibPacketType.internal("client_mod_list_request", StreamCodec.unit(INSTANCE));

	@AutoRegister
	public static final ConfigurationTaskHolder CONFIG_TASK = new ConfigurationTaskHolder(VidLib.id("mod_list"), (channelInfo, registry) -> registry.register(ModListRequestPayload::config));

	private static void config(VLConfigurationTask.Context ctx) {
		ctx.send(ModListRequestPayload.INSTANCE.toConfigS2C());
	}

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var list = new ArrayList<ClientModInfo>();

		for (var mod : ModList.get().getMods()) {
			list.add(new ClientModInfo(mod.getModId(), mod.getDisplayName(), mod.getVersion().toString(), mod.getOwningFile().getFile().getFileName()));
		}

		ctx.listener().send(new ClientModListPayload(list).toConfigC2S());
	}
}
