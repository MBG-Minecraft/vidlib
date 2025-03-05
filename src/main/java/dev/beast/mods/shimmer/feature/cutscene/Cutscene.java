package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.util.JsonCodecReloadListener;
import dev.beast.mods.shimmer.util.registry.RegistryReference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cutscene {
	public static final Codec<Cutscene> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		CutsceneStep.CODEC.listOf().optionalFieldOf("steps", List.of()).forGetter(c -> c.steps),
		Codec.BOOL.optionalFieldOf("allow_movement", false).forGetter(c -> c.allowMovement),
		Codec.BOOL.optionalFieldOf("open_previous_screen", false).forGetter(c -> c.openPreviousScreen)
	).apply(instance, Cutscene::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Cutscene> STREAM_CODEC = StreamCodec.composite(
		CutsceneStep.STREAM_CODEC.apply(ByteBufCodecs.list()),
		c -> c.steps,
		ByteBufCodecs.BOOL,
		c -> c.allowMovement,
		ByteBufCodecs.BOOL,
		c -> c.openPreviousScreen,
		Cutscene::new
	);

	public static final RegistryReference.Holder<ResourceLocation, Cutscene> SERVER = RegistryReference.createServerHolder();

	public static class Loader extends JsonCodecReloadListener<Cutscene> {
		public Loader() {
			super("shimmer/cutscene", CODEC, false);
		}

		@Override
		protected void apply(Map<ResourceLocation, Cutscene> from) {
			SERVER.update(Map.copyOf(from));
		}
	}

	public final List<CutsceneStep> steps;
	public boolean allowMovement;
	public boolean openPreviousScreen;
	public List<CutsceneTick> tick;

	public static Cutscene create() {
		return new Cutscene(new ArrayList<>(2), false, false);
	}

	private Cutscene(List<CutsceneStep> steps, boolean allowMovement, boolean openPreviousScreen) {
		this.steps = steps;
		this.allowMovement = allowMovement;
		this.openPreviousScreen = openPreviousScreen;
	}

	public List<CutsceneStep> steps() {
		return steps;
	}

	public Cutscene step(CutsceneStep step) {
		steps.add(step);
		return this;
	}

	public Cutscene allowMovement() {
		this.allowMovement = true;
		return this;
	}

	public Cutscene openPreviousScreen() {
		this.openPreviousScreen = true;
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
