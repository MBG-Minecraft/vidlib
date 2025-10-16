package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record ProfileEntityFilter(GameProfile profile) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static SimpleRegistryType<ProfileEntityFilter> TYPE = SimpleRegistryType.dynamic("profile", RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.GAME_PROFILE.fieldOf("profile").forGetter(ProfileEntityFilter::profile)
	).apply(instance, ProfileEntityFilter::new)), ByteBufCodecs.GAME_PROFILE.map(ProfileEntityFilter::new, ProfileEntityFilter::profile));

	public static class Builder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = ImBuilderHolder.of("Profile", Builder::new);

		public final GameProfileImBuilder profile = new GameProfileImBuilder();

		@Override
		public ImBuilderHolder<?> holder() {
			return TYPE;
		}

		@Override
		public void set(EntityFilter value) {
			if (value instanceof ProfileEntityFilter f) {
				profile.set(f.profile);
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			return profile.imgui(graphics);
		}

		@Override
		public boolean isValid() {
			return profile.isValid();
		}

		@Override
		public EntityFilter build() {
			return new ProfileEntityFilter(profile.build());
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.getUUID().equals(profile.getId()) || entity.getScoreboardName().equalsIgnoreCase(profile.getName());
	}

	@Override
	@Nullable
	public Entity getFirst(Level level) {
		var entity = level.getEntityByUUID(profile.getId());

		if (entity != null) {
			return entity;
		}

		for (var e : level.allEntities()) {
			if (e.getScoreboardName().equalsIgnoreCase(profile.getName())) {
				return e;
			}
		}

		return null;
	}

	@Override
	public ImBuilderWithHolder<?> createImBuilder() {
		return new Builder();
	}
}
