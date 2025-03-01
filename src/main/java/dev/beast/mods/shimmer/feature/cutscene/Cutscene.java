package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cutscene {
	public static final Codec<Cutscene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		CutsceneStep.CODEC.listOf().optionalFieldOf("steps", List.of()).forGetter(Cutscene::steps)
	).apply(instance, Cutscene::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Cutscene> STREAM_CODEC = CutsceneStep.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Cutscene::new, Cutscene::steps);

	public static Map<ResourceLocation, Cutscene> SERVER = Map.of();

	public final List<CutsceneStep> steps;
	public List<CutsceneTick> tick;

	public static Cutscene create() {
		return new Cutscene();
	}

	private Cutscene(List<CutsceneStep> steps) {
		this.steps = steps;
	}

	private Cutscene() {
		this(new ArrayList<>(2));
	}

	public List<CutsceneStep> steps() {
		return steps;
	}

	public Cutscene step(CutsceneStep step) {
		steps.add(step);
		return this;
	}

	public Cutscene tick(CutsceneTick tick) {
		if (this.tick == null) {
			this.tick = new ArrayList<>(1);
		}

		this.tick.add(tick);
		return this;
	}
}
