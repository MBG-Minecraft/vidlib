package dev.latvian.mods.vidlib.feature.entity;

import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

public class MobEffectImBuilder implements ImBuilder<Holder<MobEffect>> {
	public static final Lazy<Holder<MobEffect>[]> MOB_EFFECTS = Lazy.of(() -> BuiltInRegistries.MOB_EFFECT.stream().map(BuiltInRegistries.MOB_EFFECT::wrapAsHolder).toArray(Holder[]::new));

	public final Holder<MobEffect>[] effect = new Holder[1];

	public MobEffectImBuilder(@Nullable Holder<MobEffect> defaultEffect) {
		effect[0] = defaultEffect;
	}

	@Override
	public void set(Holder<MobEffect> value) {
		effect[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return graphics.combo("###mob-effect", "Select Effect...", effect, MOB_EFFECTS.get(), e -> I18n.get(e.value().getDescriptionId()), 0);
	}

	@Override
	public boolean isValid() {
		return effect[0] != null;
	}

	@Override
	public Holder<MobEffect> build() {
		return effect[0];
	}
}
