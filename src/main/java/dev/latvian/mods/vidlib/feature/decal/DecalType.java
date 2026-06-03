package dev.latvian.mods.vidlib.feature.decal;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DecalType implements StringRepresentable {
	// Static types
	NONE("none", 0),
	REGULAR("regular", 1),
	SPHERE("sphere", 2),
	CYLINDER("cylinder", 3),
	// FILLED
	FILLED_CYLINDER("filled_cylinder", 5),
	CUBE("cube", 6),
	// Dynamic types
	DANGER("danger", -1),

	;

	public static final DecalType[] VALUES = values();
	public static final DecalType[] UNIT = {NONE};
	public static final Codec<DecalType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, DecalType> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);
	public static final DataType<DecalType> DATA_TYPE = DataType.of(CODEC, STREAM_CODEC, DecalType.class);

	public final String name;
	public final int shaderId;

	DecalType(String name, int shaderId) {
		this.name = name;
		this.shaderId = shaderId;
	}

	@Override
	public @NotNull String getSerializedName() {
		return name;
	}
}
