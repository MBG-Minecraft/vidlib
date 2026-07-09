package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.registry.DynamicType;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWithHolder;
import dev.latvian.mods.vidlib.feature.registry.CustomRegistryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record ProfileEntityFilter(GameProfile profile) implements EntityFilter, ImBuilderWithHolder.Factory {
	public static DynamicType<RegistryFriendlyByteBuf, EntityFilter> TYPE = CustomRegistryType.dynamic("profile", RecordCodecBuilder.mapCodec(instance -> instance.group(
		DataTypes.GAME_PROFILE.codec().fieldOf("profile").forGetter(ProfileEntityFilter::profile)
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
	public DynamicType<RegistryFriendlyByteBuf, EntityFilter> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entity.getUUID().equals(profile.id()) || entity.getScoreboardName().equalsIgnoreCase(profile.name());
	}

	@Override
	@Nullable
	public Entity getFirst(Level level) {
		var entity = level.getEntityByUUID(profile.id());

		if (entity != null) {
			return entity;
		}

		for (var e : level.allEntities()) {
			if (e.getScoreboardName().equalsIgnoreCase(profile.name())) {
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
