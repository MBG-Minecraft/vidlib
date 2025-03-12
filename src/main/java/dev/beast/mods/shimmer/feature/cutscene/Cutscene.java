package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
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
		Codec.BOOL.optionalFieldOf("open_previous_screen", false).forGetter(c -> c.openPreviousScreen),
		Codec.BOOL.optionalFieldOf("hide_player", false).forGetter(c -> c.hidePlayer)
	).apply(instance, Cutscene::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Cutscene> STREAM_CODEC = CompositeStreamCodec.of(
		CutsceneStep.STREAM_CODEC.list(),
		c -> c.steps,
		ByteBufCodecs.BOOL,
		c -> c.allowMovement,
		ByteBufCodecs.BOOL,
		c -> c.openPreviousScreen,
		ByteBufCodecs.BOOL,
		c -> c.hidePlayer,
		Cutscene::new
	);

	public static final KnownCodec<Cutscene> KNOWN_CODEC = KnownCodec.register(Shimmer.id("cutscene"), CODEC, Cutscene.class);

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
	public boolean hidePlayer;

	public static Cutscene create() {
		return new Cutscene(new ArrayList<>(2), false, false, false);
	}

	private Cutscene(List<CutsceneStep> steps, boolean allowMovement, boolean openPreviousScreen, boolean hidePlayer) {
		this.steps = steps;
		this.allowMovement = allowMovement;
		this.openPreviousScreen = openPreviousScreen;
		this.hidePlayer = hidePlayer;
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

	public Cutscene hidePlayer() {
		this.hidePlayer = true;
		return this;
	}
}
