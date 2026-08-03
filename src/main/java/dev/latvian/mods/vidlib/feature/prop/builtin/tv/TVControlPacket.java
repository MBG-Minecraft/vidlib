package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record TVControlPacket(int control) implements SimplePacketPayload {
	public static final int STOP = 0;
	public static final int PLAY = 1;
	public static final int PAUSE = 2;
	public static final int RESET = 3;
	public static final int MUTE = 4;
	public static final int UNMUTE = 5;

	@AutoPacket
	public static final VidLibPacketType<TVControlPacket> TYPE = VidLibPacketType.internal("tv/control", ByteBufCodecs.VAR_INT.map(TVControlPacket::new, TVControlPacket::control));

	@Override
	public VidLibPacketType<?> getType() {
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