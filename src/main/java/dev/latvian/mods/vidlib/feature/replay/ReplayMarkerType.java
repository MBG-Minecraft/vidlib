package dev.latvian.mods.vidlib.feature.replay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

public enum ReplayMarkerType implements StringRepresentable {
	PACKET("packet"),
	USER("user"),
	API("api");

	public static final Codec<ReplayMarkerType> CODEC = KLibCodecs.anyEnumCodec(values());

	private final String name;
	public final Codec<ReplayMarkerData> codec;

	ReplayMarkerType(String name) {
		this.name = name;
		this.codec = RecordCodecBuilder.create(instance -> instance.group(
			Color.SOLID_CODEC.optionalFieldOf("color", Color.RED).forGetter(ReplayMarkerData::color),
			MarkerPosition.CODEC.optionalFieldOf("position").forGetter(ReplayMarkerData::position),
			Codec.STRING.optionalFieldOf("description", "").forGetter(ReplayMarkerData::description)
		).apply(instance, this::make));
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public ReplayMarkerData make(Color color, Optional<MarkerPosition> position, String description) {
		return new ReplayMarkerData(this, color, position, description);
	}

	public ReplayMarkerData make(Color color, String description) {
		return make(color, Optional.empty(), description);
	}
}
