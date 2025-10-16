package dev.latvian.mods.vidlib.feature.cutscene.step;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

public enum CutsceneStepType implements StringRepresentable {
	ORIGIN("origin", ImIcons.LOCATION, "Origin", true, OriginCutsceneStep::new, OriginCutsceneStep.CODEC, OriginCutsceneStep.STREAM_CODEC),
	TARGET("target", ImIcons.TARGET, "Target", true, TargetCutsceneStep::new, TargetCutsceneStep.CODEC, TargetCutsceneStep.STREAM_CODEC),
	FOV_MODIFIER("fov_modifier", ImIcons.APERTURE, "FOV Modifier", true, FOVModifierCutsceneStep::new, FOVModifierCutsceneStep.CODEC, FOVModifierCutsceneStep.STREAM_CODEC),
	STATUS_TEXT("status_text", ImIcons.TEXT, "Status Text", true, StatusTextCutsceneStep::new, StatusTextCutsceneStep.CODEC, StatusTextCutsceneStep.STREAM_CODEC),
	BAR_VISIBILITY("bar_visibility", ImIcons.SPLIT_SCREEN, "Bar Visibility", true, BarVisibilityCutsceneStep::new, BarVisibilityCutsceneStep.CODEC, BarVisibilityCutsceneStep.STREAM_CODEC),
	TOP_BAR_TEXT("top_bar_text", ImIcons.TEXT, "Top Bar Text", false, TopBarTextCutsceneStep::new, TopBarTextCutsceneStep.CODEC, TopBarTextCutsceneStep.STREAM_CODEC),
	BOTTOM_BAR_TEXT("bottom_bar_text", ImIcons.TEXT, "Bottom Bar Text", false, BottomBarTextCutsceneStep::new, BottomBarTextCutsceneStep.CODEC, BottomBarTextCutsceneStep.STREAM_CODEC),
	SCREEN_EFFECT("screen_effect", ImIcons.BLUR, "Screen Effect", false, ScreenEffectCutsceneStep::new, ScreenEffectCutsceneStep.CODEC, ScreenEffectCutsceneStep.STREAM_CODEC),
	SOUND("sound", ImIcons.PLAY, "Sound", false, SoundCutsceneStep::new, SoundCutsceneStep.CODEC, SoundCutsceneStep.STREAM_CODEC),
	CUSTOM_EVENT("custom_event", ImIcons.CODE, "Custom Event", false, CustomEventCutsceneStep::new, CustomEventCutsceneStep.CODEC, CustomEventCutsceneStep.STREAM_CODEC);

	public static final CutsceneStepType[] VALUES = CutsceneStepType.values();
	public static final DataType<CutsceneStepType> DATA_TYPE = DataType.of(VALUES);
	public static final StreamCodec<RegistryFriendlyByteBuf, CutsceneStepType> STREAM_CODEC = Cast.to(DATA_TYPE.streamCodec());

	private final String name;
	public final ImIcon icon;
	public final String displayName;
	public final boolean hasSnap;
	public final Supplier<CutsceneStep> factory;
	public final MapCodec<CutsceneStep> mapCodec;
	public final StreamCodec<RegistryFriendlyByteBuf, CutsceneStep> streamCodec;

	<T extends CutsceneStep> CutsceneStepType(String name, ImIcon icon, String displayName, boolean hasSnap, Supplier<T> factory, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		this.name = name;
		this.icon = icon;
		this.displayName = displayName;
		this.hasSnap = hasSnap;
		this.factory = Cast.to(factory);

		this.mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("start").forGetter(o -> o.start),
			Codec.INT.optionalFieldOf("length", 0).forGetter(o -> o.length),
			Codec.BOOL.optionalFieldOf("snap", true).forGetter(o -> o.snap),
			codec.fieldOf("value").forGetter(Cast::to)
		).apply(instance, (start, length, snap, value) -> {
			value.start = start;
			value.length = length;
			value.snap = snap;
			return value;
		}));

		this.streamCodec = CompositeStreamCodec.of(
			ByteBufCodecs.VAR_INT, o -> o.start,
			ByteBufCodecs.VAR_INT, o -> o.length,
			ByteBufCodecs.VAR_INT, CutsceneStep::getFlags,
			Cast.to(streamCodec), o -> o,
			(start, length, flags, value) -> {
				value.start = start;
				value.length = length;
				value.setFlags(flags);
				return value;
			}
		);
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
