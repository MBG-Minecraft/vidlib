package dev.latvian.mods.vidlib.feature.decal;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DecalFillType implements StringRepresentable {
	SOLID(0, "solid", "Solid"),
	GRID(1, "grid", "Grid"),
	DIAGONAL(2, "diagonal", "Diagonal"),
	ANIMATED_DIAGONAL(3, "animated_diagonal", "Animated Diagonal"),

	;

	public static final DecalFillType[] VALUES = values();
	public static final DecalFillType[] UNIT = {SOLID};
	public static final Codec<DecalFillType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, DecalFillType> STREAM_CODEC = KLibStreamCodecs.enumValue(VALUES);

	public final int shaderId;
	public final String name;
	public final String displayName;

	DecalFillType(int shaderId, String name, String displayName) {
		this.shaderId = shaderId;
		this.name = name;
		this.displayName = displayName;
	}

	@Override
	public @NotNull String getSerializedName() {
		return name;
	}
}
