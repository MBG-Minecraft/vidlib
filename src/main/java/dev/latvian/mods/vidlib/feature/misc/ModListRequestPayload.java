package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.net.ConfigurationTaskHolder;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VLConfigurationTask;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import net.minecraft.network.codec.StreamCodec;

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
		ctx.sessionData().setClientModListSentDuringConfig();
		ctx.send(new ClientModListPayload(PlatformHelper.CURRENT.getModList()).toConfigC2S());
	}
}
