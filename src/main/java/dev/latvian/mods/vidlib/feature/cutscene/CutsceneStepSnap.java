package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public record CutsceneStepSnap(boolean origin, boolean target, boolean fov) {
	public static final CutsceneStepSnap NONE = new CutsceneStepSnap(false);
	public static final CutsceneStepSnap ALL = new CutsceneStepSnap(true);

	public static final Codec<CutsceneStepSnap> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("origin", false).forGetter(CutsceneStepSnap::origin),
		Codec.BOOL.optionalFieldOf("target", false).forGetter(CutsceneStepSnap::target),
		Codec.BOOL.optionalFieldOf("fov", false).forGetter(CutsceneStepSnap::fov)
	).apply(instance, CutsceneStepSnap::new));

	public static final StreamCodec<ByteBuf, CutsceneStepSnap> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public CutsceneStepSnap decode(ByteBuf buf) {
			int flags = buf.readByte() & 0xFF;
			return flags == 0 ? NONE : new CutsceneStepSnap(
				(flags & 1) != 0,
				(flags & 2) != 0,
				(flags & 4) != 0
			);
		}

		@Override
		public void encode(ByteBuf buf, CutsceneStepSnap value) {
			buf.writeByte((value.origin() ? 1 : 0) | (value.target() ? 2 : 0) | (value.fov() ? 4 : 0));
		}
	};

	public static final Codec<CutsceneStepSnap> CODEC = Codec.either(Codec.BOOL, DIRECT_CODEC).xmap(either -> either.map(v -> v ? ALL : NONE, Function.identity()), Either::right);

	private CutsceneStepSnap(boolean value) {
		this(value, value, value);
	}
}
