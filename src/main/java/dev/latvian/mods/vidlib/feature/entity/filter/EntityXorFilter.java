package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.world.entity.Entity;

public record EntityXorFilter(EntityFilter a, EntityFilter b) implements EntityFilter, ImBuilderWrapper.BuilderSupplier {
	public static SimpleRegistryType<EntityXorFilter> TYPE = SimpleRegistryType.dynamic("xor", RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityFilter.CODEC.fieldOf("a").forGetter(EntityXorFilter::a),
		EntityFilter.CODEC.fieldOf("b").forGetter(EntityXorFilter::b)
	).apply(instance, EntityXorFilter::new)), CompositeStreamCodec.of(
		EntityFilter.STREAM_CODEC, EntityXorFilter::a,
		EntityFilter.STREAM_CODEC, EntityXorFilter::b,
		EntityXorFilter::new
	));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("XOR", Builder::new);

		public final ImBuilder<EntityFilter> a = EntityFilterImBuilder.create();
		public final ImBuilder<EntityFilter> b = EntityFilterImBuilder.create();

		@Override
		public void set(EntityFilter value) {
			if (value instanceof EntityXorFilter f) {
				a.set(f.a);
				b.set(f.b);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(a.imguiKey(graphics, "A", "a"));
			update = update.or(b.imguiKey(graphics, "B", "b"));
			return update;
		}

		@Override
		public boolean isValid() {
			return a.isValid() && b.isValid();
		}

		@Override
		public EntityFilter build() {
			return new EntityXorFilter(a.build(), b.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return a.test(entity) ^ b.test(entity);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return Builder.TYPE;
	}
}
