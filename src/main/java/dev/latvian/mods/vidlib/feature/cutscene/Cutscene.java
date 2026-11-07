package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.registry.RefType;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStep;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record Cutscene(
	boolean allowMovement,
	boolean openPreviousScreen,
	boolean hidePlayer,
	List<CutsceneStep> steps
) {
	public static final DynamicType<RegistryFriendlyByteBuf, Cutscene> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.BOOL.optionalFieldOf("allow_movement", false).forGetter(Cutscene::allowMovement),
			Codec.BOOL.optionalFieldOf("open_previous_screen", false).forGetter(Cutscene::openPreviousScreen),
			Codec.BOOL.optionalFieldOf("hide_player", false).forGetter(Cutscene::hidePlayer),
			CutsceneStep.CODEC.listOf().optionalFieldOf("steps", List.of()).forGetter(Cutscene::steps)
		).apply(i, Cutscene::new)),
		CompositeStreamCodec.of(
			ByteBufCodecs.VAR_INT, Cutscene::getFlags,
			KLibStreamCodecs.listOf(CutsceneStep.STREAM_CODEC), c -> c.steps,
			Cutscene::new
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, Cutscene> REGISTRY = CustomRegistry.create("cutscene", TYPE);

	public static final Codec<Ref<Cutscene>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<Cutscene>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<Cutscene>> DATA_TYPE = REGISTRY.dataType();

	public static class Loader extends JsonRegistryReloadListener<Cutscene> {
		public Loader(RefType refType) {
			super("vidlib/cutscene", REGISTRY, refType);
		}
	}

	public Cutscene(int flags, List<CutsceneStep> steps) {
		this(
			(flags & 1) != 0,
			(flags & 2) != 0,
			(flags & 4) != 0,
			steps
		);
	}

	public int getFlags() {
		int f = 0;

		if (allowMovement) {
			f |= 1;
		}

		if (openPreviousScreen) {
			f |= 2;
		}

		if (hidePlayer) {
			f |= 4;
		}

		return f;
	}
}
