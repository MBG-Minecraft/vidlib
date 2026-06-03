package dev.latvian.mods.vidlib.feature.decal;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import io.netty.buffer.Unpooled;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.List;

public class DecalImBuilder implements ImBuilder<Decal> {
	public static final ImBuilderType<Decal> TYPE = DecalImBuilder::new;

	private Decal decal;
	private byte[] snapshot;

	@Override
	public void set(Decal value) {
		this.decal = value;
		this.snapshot = encode(value);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		if (decal == null) {
			decal = new Decal(new Vector3d());
		}

		decal.imgui(graphics, List.of());

		var before = snapshot;
		snapshot = encode(decal);
		return Arrays.equals(before, snapshot) ? ImUpdate.NONE : ImUpdate.FULL;
	}

	@Override
	public Decal build() {
		return decal;
	}

	private static byte[] encode(Decal d) {
		if (d == null) {
			return new byte[0];
		}

		var buf = Unpooled.buffer();
		try {
			Decal.STREAM_CODEC.encode(buf, d);
			var bytes = new byte[buf.readableBytes()];
			buf.getBytes(buf.readerIndex(), bytes);
			return bytes;
		} finally {
			buf.release();
		}
	}
}
