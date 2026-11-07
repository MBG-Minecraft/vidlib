package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.net.ConfigurationTaskHolder;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.net.VLConfigurationTask;
import net.minecraft.network.codec.StreamCodec;

public enum ModListRequestPayload implements SimplePayload {
	INSTANCE;

	@AutoPacket(stage = AutoPacket.Stage.CONFIG)
	public static final SimplePayloadType<ModListRequestPayload> TYPE = SimplePayloadType.vidlib("client_mod_list_request", StreamCodec.unit(INSTANCE));

	@AutoRegister
	public static final ConfigurationTaskHolder CONFIG_TASK = new ConfigurationTaskHolder(ID.vidlib("mod_list"), (channelInfo, registry) -> registry.register(ModListRequestPayload::config));

	private static void config(VLConfigurationTask.Context ctx) {
		ctx.send(ModListRequestPayload.INSTANCE.toS2CPacket());
	}

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.playerSessionData().setClientModListSentDuringConfig();
		ctx.send(new ClientModListPayload(PlatformHelper.INSTANCE.getModList()).toC2SPacket());
	}
}
