package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.util.JsonRegistryReloadListener;
import dev.beast.mods.shimmer.util.registry.ShimmerRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class Cutscene {
	public static final Codec<Cutscene> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		CutsceneStep.CODEC.listOf().optionalFieldOf("steps", List.of()).forGetter(c -> c.steps),
		Codec.BOOL.optionalFieldOf("allow_movement", false).forGetter(c -> c.allowMovement),
		Codec.BOOL.optionalFieldOf("open_previous_screen", false).forGetter(c -> c.openPreviousScreen),
		Codec.BOOL.optionalFieldOf("hide_player", false).forGetter(c -> c.hidePlayer)
	).apply(instance, Cutscene::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Cutscene> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
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

	public static final KnownCodec<Cutscene> DIRECT_KNOWN_CODEC = KnownCodec.register(Shimmer.id("direct_cutscene"), DIRECT_CODEC, DIRECT_STREAM_CODEC, Cutscene.class);
	public static final ShimmerRegistry<Cutscene> REGISTRY = ShimmerRegistry.createServer("cutscene", false);
	public static final KnownCodec<Cutscene> KNOWN_CODEC = KnownCodec.of(REGISTRY, Cutscene.class);
	public static final StreamCodec<? super RegistryFriendlyByteBuf, Cutscene> STREAM_CODEC = REGISTRY.streamCodecOrDirect(KNOWN_CODEC, DIRECT_STREAM_CODEC);

	public static class Loader extends JsonRegistryReloadListener<Cutscene> {
		public Loader() {
			super("shimmer/cutscene", DIRECT_CODEC, false, REGISTRY);
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
