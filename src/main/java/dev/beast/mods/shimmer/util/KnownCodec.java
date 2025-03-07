package dev.beast.mods.shimmer.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.feature.misc.CodecArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record KnownCodec<T>(ResourceLocation id, Codec<T> codec, Class<T> type) {
	public static final Map<ResourceLocation, KnownCodec<?>> MAP = new HashMap<>();

	public static <T> KnownCodec<T> register(ResourceLocation id, Codec<T> codec, Class<T> type) {
		var knownCodec = new KnownCodec<>(id, codec, type);
		MAP.put(id, knownCodec);
		return knownCodec;
	}

	public static final KnownCodec<Cutscene> CUTSCENE = register(Shimmer.id("cutscene"), Cutscene.CODEC, Cutscene.class);
	public static final KnownCodec<CameraShake> CAMERA_SHAKE = register(Shimmer.id("camera_shake"), CameraShake.CODEC, CameraShake.class);
	public static final KnownCodec<EntityFilter> ENTITY_FILTER = register(Shimmer.id("entity_filter"), EntityFilter.CODEC, EntityFilter.class);
	public static final KnownCodec<BlockFilter> BLOCK_FILTER = register(Shimmer.id("block_filter"), BlockFilter.CODEC, BlockFilter.class);

	public static void bootstrap() {
	}

	public CodecArgument<T> argument(CommandBuildContext commandBuildContext) {
		return new CodecArgument<>(commandBuildContext.createSerializationContext(NbtOps.INSTANCE), this);
	}

	public <S> T get(CommandContext<S> context, String name) {
		return context.getArgument(name, type);
	}
}
