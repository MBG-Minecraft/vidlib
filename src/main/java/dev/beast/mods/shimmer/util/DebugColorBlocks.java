package dev.beast.mods.shimmer.util;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.math.KMath;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum DebugColorBlocks implements StringRepresentable {
	NONE("none"),

	CONCRETE("concrete",
		Blocks.BLACK_CONCRETE.defaultBlockState(),
		Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
		Blocks.GRAY_CONCRETE.defaultBlockState(),
		Blocks.BROWN_CONCRETE.defaultBlockState(),
		Blocks.BLUE_CONCRETE.defaultBlockState(),
		Blocks.CYAN_CONCRETE.defaultBlockState(),
		Blocks.GREEN_CONCRETE.defaultBlockState(),
		Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
		Blocks.LIME_CONCRETE.defaultBlockState(),
		Blocks.YELLOW_CONCRETE.defaultBlockState(),
		Blocks.ORANGE_CONCRETE.defaultBlockState(),
		Blocks.MAGENTA_CONCRETE.defaultBlockState(),
		Blocks.PURPLE_CONCRETE.defaultBlockState(),
		Blocks.RED_CONCRETE.defaultBlockState(),
		Blocks.PINK_CONCRETE.defaultBlockState(),
		Blocks.WHITE_CONCRETE.defaultBlockState()
	),

	CONCRETE_POWDER("concrete_powder",
		Blocks.BLACK_CONCRETE_POWDER.defaultBlockState(),
		Blocks.LIGHT_GRAY_CONCRETE_POWDER.defaultBlockState(),
		Blocks.GRAY_CONCRETE_POWDER.defaultBlockState(),
		Blocks.BROWN_CONCRETE_POWDER.defaultBlockState(),
		Blocks.BLUE_CONCRETE_POWDER.defaultBlockState(),
		Blocks.CYAN_CONCRETE_POWDER.defaultBlockState(),
		Blocks.GREEN_CONCRETE_POWDER.defaultBlockState(),
		Blocks.LIGHT_BLUE_CONCRETE_POWDER.defaultBlockState(),
		Blocks.LIME_CONCRETE_POWDER.defaultBlockState(),
		Blocks.YELLOW_CONCRETE_POWDER.defaultBlockState(),
		Blocks.ORANGE_CONCRETE_POWDER.defaultBlockState(),
		Blocks.MAGENTA_CONCRETE_POWDER.defaultBlockState(),
		Blocks.PURPLE_CONCRETE_POWDER.defaultBlockState(),
		Blocks.RED_CONCRETE_POWDER.defaultBlockState(),
		Blocks.PINK_CONCRETE_POWDER.defaultBlockState(),
		Blocks.WHITE_CONCRETE_POWDER.defaultBlockState()
	),

	GLAZED_TERRACOTTA("glazed_terracotta",
		Blocks.BLACK_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.GRAY_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.BROWN_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.BLUE_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.CYAN_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.GREEN_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.LIME_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.YELLOW_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.ORANGE_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.MAGENTA_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.PURPLE_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.RED_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.PINK_GLAZED_TERRACOTTA.defaultBlockState(),
		Blocks.WHITE_GLAZED_TERRACOTTA.defaultBlockState()
	),

	STAINED_GLASS("stained_glass",
		Blocks.BLACK_STAINED_GLASS.defaultBlockState(),
		Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState(),
		Blocks.GRAY_STAINED_GLASS.defaultBlockState(),
		Blocks.BROWN_STAINED_GLASS.defaultBlockState(),
		Blocks.BLUE_STAINED_GLASS.defaultBlockState(),
		Blocks.CYAN_STAINED_GLASS.defaultBlockState(),
		Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
		Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
		Blocks.LIME_STAINED_GLASS.defaultBlockState(),
		Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
		Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),
		Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(),
		Blocks.PURPLE_STAINED_GLASS.defaultBlockState(),
		Blocks.RED_STAINED_GLASS.defaultBlockState(),
		Blocks.PINK_STAINED_GLASS.defaultBlockState(),
		Blocks.WHITE_STAINED_GLASS.defaultBlockState()
	),

	WOOL("wool",
		Blocks.BLACK_WOOL.defaultBlockState(),
		Blocks.LIGHT_GRAY_WOOL.defaultBlockState(),
		Blocks.GRAY_WOOL.defaultBlockState(),
		Blocks.BROWN_WOOL.defaultBlockState(),
		Blocks.BLUE_WOOL.defaultBlockState(),
		Blocks.CYAN_WOOL.defaultBlockState(),
		Blocks.GREEN_WOOL.defaultBlockState(),
		Blocks.LIGHT_BLUE_WOOL.defaultBlockState(),
		Blocks.LIME_WOOL.defaultBlockState(),
		Blocks.YELLOW_WOOL.defaultBlockState(),
		Blocks.ORANGE_WOOL.defaultBlockState(),
		Blocks.MAGENTA_WOOL.defaultBlockState(),
		Blocks.PURPLE_WOOL.defaultBlockState(),
		Blocks.RED_WOOL.defaultBlockState(),
		Blocks.PINK_WOOL.defaultBlockState(),
		Blocks.WHITE_WOOL.defaultBlockState()
	),

	;

	public static final DebugColorBlocks[] VALUES = values();
	public static final Codec<DebugColorBlocks> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, DebugColorBlocks> STREAM_CODEC = ShimmerStreamCodecs.enumValue(VALUES);

	public final String name;
	public final BlockState[] states;

	DebugColorBlocks(String name, BlockState... states) {
		this.name = name;
		this.states = states;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public BlockState getState(int index) {
		return states[index & states.length];
	}

	public BlockState getState(double delta) {
		return states[KMath.clamp((int) (delta * (states.length - 1)), 0, states.length - 1)];
	}
}
