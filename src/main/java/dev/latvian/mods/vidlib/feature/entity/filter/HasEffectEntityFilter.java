package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.entity.MobEffectImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record HasEffectEntityFilter(Holder<MobEffect> effect) implements EntityFilter {
	public static SimpleRegistryType<HasEffectEntityFilter> TYPE = SimpleRegistryType.dynamic("has_effect", RecordCodecBuilder.mapCodec(instance -> instance.group(
		MobEffect.CODEC.fieldOf("effect").forGetter(HasEffectEntityFilter::effect)
	).apply(instance, HasEffectEntityFilter::new)), MobEffect.STREAM_CODEC.map(HasEffectEntityFilter::new, HasEffectEntityFilter::effect));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("Has Effect", Builder::new);

		public final MobEffectImBuilder effect = new MobEffectImBuilder(null);

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return effect.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return effect.isValid();
		}

		@Override
		public EntityFilter build() {
			return new HasEffectEntityFilter(effect.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity instanceof LivingEntity living && living.hasEffect(effect);
	}
}
