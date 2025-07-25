package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.registry.VLRegistry;
import dev.latvian.mods.vidlib.util.JsonRegistryReloadListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class Cutscene {
	public static final Codec<Cutscene> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("allow_movement", false).forGetter(c -> c.allowMovement),
		Codec.BOOL.optionalFieldOf("open_previous_screen", false).forGetter(c -> c.openPreviousScreen),
		Codec.BOOL.optionalFieldOf("hide_player", false).forGetter(c -> c.hidePlayer),
		CutsceneStep.CODEC.listOf().optionalFieldOf("steps", List.of()).forGetter(c -> c.steps)
	).apply(instance, Cutscene::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Cutscene> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.BOOL, c -> c.allowMovement,
		ByteBufCodecs.BOOL, c -> c.openPreviousScreen,
		ByteBufCodecs.BOOL, c -> c.hidePlayer,
		KLibStreamCodecs.listOf(CutsceneStep.STREAM_CODEC), c -> c.steps,
		Cutscene::new
	);

	public static final DataType<Cutscene> DIRECT_DATA_TYPE = DataType.of(DIRECT_CODEC, DIRECT_STREAM_CODEC, Cutscene.class);
	public static final VLRegistry<Cutscene> REGISTRY = VLRegistry.createServer("cutscene", Cutscene.class);

	public static final DataType<Cutscene> DATA_TYPE = REGISTRY.orDirect(DIRECT_DATA_TYPE);
	public static final CommandDataType<Cutscene> COMMAND = CommandDataType.of(DATA_TYPE);

	public static class Loader extends JsonRegistryReloadListener<Cutscene> {
		public Loader() {
			super("vidlib/cutscene", DIRECT_CODEC, false, REGISTRY);
		}
	}

	public boolean allowMovement;
	public boolean openPreviousScreen;
	public boolean hidePlayer;
	public final List<CutsceneStep> steps;

	public static Cutscene create() {
		return new Cutscene(false, false, false, new ArrayList<>(1));
	}

	Cutscene(boolean allowMovement, boolean openPreviousScreen, boolean hidePlayer, List<CutsceneStep> steps) {
		this.allowMovement = allowMovement;
		this.openPreviousScreen = openPreviousScreen;
		this.hidePlayer = hidePlayer;
		this.steps = steps;
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
