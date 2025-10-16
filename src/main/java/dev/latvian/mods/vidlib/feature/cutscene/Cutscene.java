package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.vidlib.feature.codec.CommandDataType;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStep;
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
	).apply(instance, (allowMovement, openPreviousScreen, hidePlayer, steps) -> {
		var c = new Cutscene();
		c.allowMovement = allowMovement;
		c.openPreviousScreen = openPreviousScreen;
		c.hidePlayer = hidePlayer;
		c.steps.addAll(steps);
		return c;
	}));

	public static final StreamCodec<RegistryFriendlyByteBuf, Cutscene> DIRECT_STREAM_CODEC = CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, Cutscene::getFlags,
		KLibStreamCodecs.listOf(CutsceneStep.STREAM_CODEC), c -> c.steps,
		(flags, steps) -> {
			var c = new Cutscene();
			c.setFlags(flags);
			c.steps.addAll(steps);
			return c;
		}
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

	public Cutscene() {
		this.allowMovement = false;
		this.openPreviousScreen = false;
		this.hidePlayer = false;
		this.steps = new ArrayList<>();
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

	public void setFlags(int flags) {
		allowMovement = (flags & 1) != 0;
		openPreviousScreen = (flags & 2) != 0;
		hidePlayer = (flags & 4) != 0;
	}
}
