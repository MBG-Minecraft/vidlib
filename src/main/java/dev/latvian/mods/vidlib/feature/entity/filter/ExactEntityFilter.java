package dev.latvian.mods.vidlib.feature.entity.filter;

import com.mojang.serialization.JavaOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.util.IntOrUUID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderHolder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderWrapper;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryType;
import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record ExactEntityFilter(IntOrUUID entityId) implements EntityFilter, ImBuilderWrapper.BuilderSupplier {
	public static SimpleRegistryType<ExactEntityFilter> TYPE = SimpleRegistryType.dynamic("exact", RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntOrUUID.DATA_TYPE.codec().fieldOf("entity_id").forGetter(ExactEntityFilter::entityId)
	).apply(instance, ExactEntityFilter::new)), IntOrUUID.DATA_TYPE.streamCodec().map(ExactEntityFilter::new, ExactEntityFilter::entityId));

	public static class IDBuilder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("Network ID", IDBuilder::new);

		public final ImInt id = new ImInt();

		@Override
		public void set(EntityFilter value) {
			if (value instanceof ExactEntityFilter f && f.entityId.optionalInt().isPresent()) {
				id.set(f.entityId.optionalInt().getAsInt());
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.inputInt("###id", id);
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return id.get() != 0;
		}

		@Override
		public EntityFilter build() {
			return new ExactEntityFilter(IntOrUUID.of(id.get()));
		}
	}

	public static class UUIDBuilder implements EntityFilterImBuilder {
		public static final ImBuilderHolder<EntityFilter> TYPE = new ImBuilderHolder<>("UUID", UUIDBuilder::new);

		public final ImString uuid = ImGuiUtils.resizableString();

		@Override
		public void set(EntityFilter value) {
			if (value instanceof ExactEntityFilter f && f.entityId.optionalUUID().isPresent()) {
				uuid.set(f.entityId.optionalUUID().get());
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			ImGui.inputText("###uuid", uuid);
			return ImUpdate.itemEdit();
		}

		@Override
		public boolean isValid() {
			return KLibCodecs.UUID.decode(JavaOps.INSTANCE, uuid.get()).isSuccess();
		}

		@Override
		public EntityFilter build() {
			return new ExactEntityFilter(IntOrUUID.of(UndashedUuid.fromStringLenient(uuid.get())));
		}
	}

	@Override
	public SimpleRegistryType<?> type() {
		return TYPE;
	}

	@Override
	public boolean test(Entity entity) {
		return entityId.testEntity(entity);
	}

	@Override
	@Nullable
	public Entity getFirst(Level level) {
		return level.getEntity(entityId);
	}

	@Override
	public ImBuilderHolder<?> getImBuilderHolder() {
		return entityId.optionalUUID().isPresent() ? UUIDBuilder.TYPE : IDBuilder.TYPE;
	}
}
