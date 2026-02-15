package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.util.Comparison;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumberImBuilder;
import dev.latvian.mods.vidlib.feature.entity.number.FixedEntityNumber;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePin;
import dev.latvian.mods.vidlib.feature.imgui.node.NodePinType;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record IfEntityFilter(EntityNumber ifValue, Comparison comparison, EntityNumber testValue) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static SimpleRegistryType<IfEntityFilter> TYPE = SimpleRegistryType.dynamic("if", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityNumber.CODEC.fieldOf("if").forGetter(IfEntityFilter::ifValue),
		Comparison.DATA_TYPE.codec().optionalFieldOf("comparison", Comparison.NOT_EQUALS).forGetter(IfEntityFilter::comparison),
		EntityNumber.CODEC.optionalFieldOf("value", FixedEntityNumber.ZERO).forGetter(IfEntityFilter::testValue)
	).apply(instance, IfEntityFilter::new)), CompositeStreamCodec.of(
		EntityNumber.STREAM_CODEC, IfEntityFilter::ifValue,
		Comparison.DATA_TYPE.streamCodec(), IfEntityFilter::comparison,
		KLibStreamCodecs.optional(EntityNumber.STREAM_CODEC, FixedEntityNumber.ZERO), IfEntityFilter::testValue,
		IfEntityFilter::new
	));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("If", Builder::new);

		public static final List<NodePin> PINS = List.of(
			NodePinType.ENTITY_NUMBER.required("If"),
			NodePinType.NUMBER.required("Test"),
			NodePinType.ENTITY_NUMBER.optional("Then")
		);

		public final ImBuilder<EntityNumber> ifValue = EntityNumberImBuilder.create(EntityNumber.of(1D));
		public final ImBuilder<Comparison> comparison = new EnumImBuilder<>(Comparison.VALUES, Comparison.NOT_EQUALS);
		public final ImBuilder<EntityNumber> testValue = EntityNumberImBuilder.create(EntityNumber.of(0D));

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof IfEntityFilter v) {
				ifValue.set(v.ifValue);
				comparison.set(v.comparison);
				testValue.set(v.testValue);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(ifValue.imguiKey(graphics, "If", "if"));
			update = update.or(comparison.imguiKey(graphics, "Comparison", "comparison"));
			update = update.or(testValue.imguiKey(graphics, "Value", "value"));
			return update;
		}

		@Override
		public ImUpdate nodeImgui(ImGraphics graphics) {
			return comparison.imguiKey(graphics, "Comparison", "comparison");
		}

		@Override
		public boolean isValid() {
			return ifValue.isValid() && comparison.isValid() && testValue.isValid();
		}

		@Override
		public EntityFilter build() {
			return new IfEntityFilter(ifValue.build(), comparison.build(), testValue.build());
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
	public boolean test(Entity entity) {
		return comparison.test(ifValue.applyAsDouble(entity), testValue.applyAsDouble(entity));
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
