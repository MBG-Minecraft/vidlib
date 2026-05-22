package dev.latvian.mods.vidlib.math.knumber;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumberImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record EntityKNumber(EntityNumber number) implements KNumber, ImBuilderWithHolder.Factory {
	public static final SimpleRegistryType<EntityKNumber> TYPE = SimpleRegistryType.dynamic("entity_number", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityNumber.CODEC.fieldOf("number").forGetter(EntityKNumber::number)
	).apply(instance, EntityKNumber::new)), CompositeStreamCodec.of(
		EntityNumber.STREAM_CODEC, EntityKNumber::number,
		EntityKNumber::new
	));

	public static class Builder implements KNumberImBuilder {
		public static final ImBuilderHolder<KNumber> TYPE = ImBuilderHolder.of("Entity Number", Builder::new);

		public static final List<NodePin> PINS = List.of(
			NodePinType.ENTITY_NUMBER.required("Number"),
			NodePinType.NUMBER.output("Out")
		);

		public final ImBuilder<EntityNumber> number = EntityNumberImBuilder.create();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(KNumber value) {
			if (value instanceof EntityKNumber n) {
				number.set(n.number);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return number.imguiKey(graphics, "Number", "number");
		}

		@Override
		public ImUpdate nodeImgui(ImGraphics graphics) {
			return ImUpdate.NONE;
		}

		@Override
		public boolean isValid() {
			return number.isValid();
		}

		@Override
		public KNumber build() {
			return new EntityKNumber(number.build());
		}

		@Override
		public List<NodePin> getNodePins() {
			return PINS;
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	@Nullable
	public Double get(KNumberContext ctx) {
		return ctx.entity == null ? null : number.applyAsDouble(ctx.entity);
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
