package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;

public record TVControlPayload(int control) implements SimplePayload {
	public static final int STOP = 0;
	public static final int PLAY = 1;
	public static final int PAUSE = 2;
	public static final int RESET = 3;
	public static final int MUTE = 4;
	public static final int UNMUTE = 5;

	@AutoPacket
	public static final SimplePayloadType<TVControlPayload> TYPE = SimplePayloadType.vidlib("tv/control", ByteBufCodecs.VAR_INT.map(TVControlPayload::new, TVControlPayload::control));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		handle();
	}

	private void handle() {
		switch (control) {
			case STOP -> TVPlayer.stop();
			case PLAY -> TVPlayer.play();
			case PAUSE -> TVPlayer.pause();
			case RESET -> TVPlayer.reset();
			case MUTE -> TVPlayer.mute();
			case UNMUTE -> TVPlayer.unmute();
		}
	}
}