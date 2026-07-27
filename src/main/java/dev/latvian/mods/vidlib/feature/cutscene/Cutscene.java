package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.vidlib.feature.cutscene.step.CutsceneStep;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class Cutscene implements CustomRegistryValue<RegistryFriendlyByteBuf, Cutscene> {
	public static final DynamicType<RegistryFriendlyByteBuf, Cutscene> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
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
		})),
		CompositeStreamCodec.of(
			ByteBufCodecs.VAR_INT, Cutscene::getFlags,
			KLibStreamCodecs.listOf(CutsceneStep.STREAM_CODEC), c -> c.steps,
			(flags, steps) -> {
				var c = new Cutscene();
				c.setFlags(flags);
				c.steps.addAll(steps);
				return c;
			}
		)
	);

	public static final CustomRegistry<RegistryFriendlyByteBuf, Cutscene> REGISTRY = CustomRegistry.create("cutscene", TYPE);

	public static final DataType<Ref<Cutscene>> DATA_TYPE = REGISTRY.dataType();
	public static final Codec<Ref<Cutscene>> CODEC = REGISTRY.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Ref<Cutscene>> STREAM_CODEC = REGISTRY.streamCodec();

	public static class ServerLoader extends JsonRegistryReloadListener<Cutscene> {
		public ServerLoader() {
			super("vidlib/cutscene", REGISTRY);
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

	@Override
	public CustomRegistry<RegistryFriendlyByteBuf, Cutscene> getRegistry() {
		return REGISTRY;
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
