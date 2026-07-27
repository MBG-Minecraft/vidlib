package dev.latvian.mods.vidlib.feature.clock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.JOMLCodecs;
import dev.latvian.mods.klib.codec.JOMLStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.CustomRegistryValue;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.JsonRegistryReloadListener;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector2i;

import java.util.List;

public record ClockFont(
	Ref<ClockFont> ref,
	ClientAsset.ResourceTexture assetId,
	Vector2i size,
	Vector2i textureSize,
	int separatorWidth,
	int actualSeparatorWidth,
	List<UV> uvs
) implements CustomRegistryValue<ByteBuf, ClockFont> {
	public static ClockFont create(Ref<ClockFont> ref,
	                               ClientAsset.ResourceTexture assetId,
	                               Vector2i size,
	                               Vector2i textureSize,
	                               int separatorWidth
	) {
		var uvs = new UV[11];
		int actualSeparatorWidth = separatorWidth > 0 ? separatorWidth : size.x();
		float woff = size.x() + 1F;

		for (int i = 0; i < 11; i++) {
			float w = i == 10 ? actualSeparatorWidth : size.x();
			float h = size.y();
			float tw = textureSize.x();
			float th = textureSize.y();

			uvs[i] = new UV(
				(i * woff) / tw,
				0F,
				((i * woff) + w) / tw,
				h / th
			);
		}

		return new ClockFont(ref, assetId, size, textureSize, separatorWidth, actualSeparatorWidth, List.of(uvs));
	}

	public static final DynamicType<ByteBuf, ClockFont> TYPE = DynamicType.create(
		"default",
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ref.<ClockFont>contextRefCodec().forGetter(ClockFont::ref),
			ClientAsset.ResourceTexture.DEFAULT_FIELD_CODEC.forGetter(ClockFont::assetId),
			JOMLCodecs.IVEC2S.fieldOf("size").forGetter(ClockFont::size),
			JOMLCodecs.IVEC2S.fieldOf("texture_size").forGetter(ClockFont::textureSize),
			Codec.INT.optionalFieldOf("separator_width", 0).forGetter(ClockFont::separatorWidth)
		).apply(instance, ClockFont::create)),
		CompositeStreamCodec.of(
			Ref.contextRefStreamCodec(), ClockFont::ref,
			ClientAsset.ResourceTexture.STREAM_CODEC, ClockFont::assetId,
			JOMLStreamCodecs.IVEC2, ClockFont::size,
			JOMLStreamCodecs.IVEC2, ClockFont::textureSize,
			ByteBufCodecs.VAR_INT, ClockFont::separatorWidth,
			ClockFont::create
		)
	);

	public static final CustomRegistry<ByteBuf, ClockFont> REGISTRY = CustomRegistry.createNoValueSync("clock_font", TYPE);

	public static final Codec<Ref<ClockFont>> CODEC = REGISTRY.codec();
	public static final StreamCodec<ByteBuf, Ref<ClockFont>> STREAM_CODEC = REGISTRY.streamCodec();
	public static final DataType<Ref<ClockFont>> DATA_TYPE = REGISTRY.dataType();
	public static final ImBuilderType<Ref<ClockFont>> IM_TYPE = () -> EnumImBuilder.of(REGISTRY).build();

	public static class ClientLoader extends JsonRegistryReloadListener<ClockFont> {
		public ClientLoader() {
			super("vidlib/clock_font", REGISTRY);
		}
	}

	@Override
	public CustomRegistry<ByteBuf, ClockFont> getRegistry() {
		return REGISTRY;
	}

	public int getWidth(char[] string) {
		if (string.length == 0) {
			return 0;
		}

		int w = 1;

		for (char c : string) {
			if (c == ':') {
				w += separatorWidth + 1;
			} else {
				w += size.x() + 1;
			}
		}

		return w;
	}
}
