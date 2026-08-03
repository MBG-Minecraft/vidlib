package dev.latvian.mods.vidlib.feature.prop.builtin.tv;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.fml.loading.FMLPaths;

import java.net.URI;

public record TVPlayPayload(Either<String, String> path, int flags) implements SimplePacketPayload {
	public static final int START_PAUSED = 1;
	public static final int LOOPING = 2;
	public static final int MUTED = 4;

	public static TVPlayPayload uri(String uri, int flags) {
		return new TVPlayPayload(Either.left(uri), flags);
	}

	public static TVPlayPayload file(String file, int flags) {
		return new TVPlayPayload(Either.right(file), flags);
	}

	@AutoPacket
	public static final VidLibPacketType<TVPlayPayload> TYPE = VidLibPacketType.internal("tv/play", CompositeStreamCodec.of(
		ByteBufCodecs.either(ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), TVPlayPayload::path,
		ByteBufCodecs.VAR_INT, TVPlayPayload::flags,
		TVPlayPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		play();
	}

	private void play() {
		TVPlayer.start(
			path.map(URI::create, file -> FMLPaths.GAMEDIR.get().resolve(file).toUri()),
			(flags & START_PAUSED) != 0,
			(flags & LOOPING) != 0,
			(flags & MUTED) != 0
		);
	}
}
