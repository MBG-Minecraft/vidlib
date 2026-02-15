package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.interpolation.InterpolationType;
import dev.latvian.mods.vidlib.feature.block.filter.BlockAndFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterImBuilderEvent;
import dev.latvian.mods.vidlib.feature.block.filter.BlockIdFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockNotFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockOrFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockStateFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockTypeTagFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockXorFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityAndFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterImBuilderEvent;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityNotFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityOrFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityTagFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityTypeFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityTypeTagFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityXorFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.ExactEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.HasEffectEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.HasItemEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.IfEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.InDimensionEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.MatchEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.PlayerDataEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.ProfileEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.ServerDataEntityFilter;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumberImBuilderEvent;
import dev.latvian.mods.vidlib.feature.imgui.builder.interpolation.InterpolationImBuilderEvent;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.BlockParticleOptionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ColorParticleOptionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.DustParticleOptionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.particle.ParticleOptionsImBuilderRegistryEvent;
import dev.latvian.mods.vidlib.feature.particle.VidLibParticles;
import dev.latvian.mods.vidlib.feature.particle.WindParticleOptionsImBuilder;
import dev.latvian.mods.vidlib.math.knumber.Atan2KNumber;
import dev.latvian.mods.vidlib.math.knumber.ClampedKNumber;
import dev.latvian.mods.vidlib.math.knumber.CosKNumber;
import dev.latvian.mods.vidlib.math.knumber.EntityKNumber;
import dev.latvian.mods.vidlib.math.knumber.FixedKNumber;
import dev.latvian.mods.vidlib.math.knumber.IfKNumber;
import dev.latvian.mods.vidlib.math.knumber.InterpolatedKNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberImBuilderEvent;
import dev.latvian.mods.vidlib.math.knumber.LiteralKNumber;
import dev.latvian.mods.vidlib.math.knumber.OffsetKNumber;
import dev.latvian.mods.vidlib.math.knumber.RandomKNumber;
import dev.latvian.mods.vidlib.math.knumber.ScaledKNumber;
import dev.latvian.mods.vidlib.math.knumber.ServerDataKNumber;
import dev.latvian.mods.vidlib.math.knumber.SinKNumber;
import dev.latvian.mods.vidlib.math.knumber.VariableKNumber;
import dev.latvian.mods.vidlib.math.kvector.DynamicKVector;
import dev.latvian.mods.vidlib.math.kvector.FixedKVector;
import dev.latvian.mods.vidlib.math.kvector.FollowingEntityKVector;
import dev.latvian.mods.vidlib.math.kvector.FollowingPropKVector;
import dev.latvian.mods.vidlib.math.kvector.GroundKVector;
import dev.latvian.mods.vidlib.math.kvector.IfKVector;
import dev.latvian.mods.vidlib.math.kvector.InterpolatedKVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilderEvent;
import dev.latvian.mods.vidlib.math.kvector.LiteralKVector;
import dev.latvian.mods.vidlib.math.kvector.OffsetKVector;
import dev.latvian.mods.vidlib.math.kvector.PivotingKVector;
import dev.latvian.mods.vidlib.math.kvector.ScalarKVector;
import dev.latvian.mods.vidlib.math.kvector.ScaledKVector;
import dev.latvian.mods.vidlib.math.kvector.VariableKVector;
import dev.latvian.mods.vidlib.math.kvector.YRotatedKVector;
import dev.latvian.mods.vidlib.util.StringUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = VidLib.ID)
public class ImBuilderRegistryEventHandler {
	@SubscribeEvent
	public static void particleImBuilders(ParticleOptionsImBuilderRegistryEvent event) {
		event.register(List.of(
			ParticleTypes.BLOCK,
			ParticleTypes.BLOCK_MARKER,
			ParticleTypes.FALLING_DUST,
			ParticleTypes.DUST_PILLAR,
			ParticleTypes.BLOCK_CRUMBLE
		), BlockParticleOptionImBuilder::new);

		event.register(ParticleTypes.DUST, t -> new DustParticleOptionImBuilder());

		event.register(List.of(
			ParticleTypes.ENTITY_EFFECT,
			ParticleTypes.TINTED_LEAVES
		), ColorParticleOptionImBuilder::new);

		event.register(VidLibParticles.WIND.get(), t -> new WindParticleOptionsImBuilder());
	}

	@SubscribeEvent
	public static void numberImBuilders(KNumberImBuilderEvent event) {
		event.add(FixedKNumber.Builder.TYPE);

		for (var literal : LiteralKNumber.values()) {
			event.addUnit(literal.displayName, literal);
		}

		event.add(InterpolatedKNumber.Builder.TYPE);
		event.add(OffsetKNumber.Builder.TYPE);
		event.add(ScaledKNumber.Builder.TYPE);
		event.add(VariableKNumber.Builder.TYPE);
		event.add(IfKNumber.Builder.TYPE);
		event.add(ServerDataKNumber.Builder.TYPE);
		event.add(RandomKNumber.Builder.TYPE);
		event.add(SinKNumber.Builder.TYPE);
		event.add(CosKNumber.Builder.TYPE);
		event.add(Atan2KNumber.Builder.TYPE);
		event.add(ClampedKNumber.Builder.TYPE);
		event.add(EntityKNumber.Builder.TYPE);
	}

	@SubscribeEvent
	public static void vectorImBuilders(KVectorImBuilderEvent event) {
		event.add(FixedKVector.Builder.TYPE);

		for (var literal : LiteralKVector.values()) {
			event.addUnit(literal.displayName, literal);
		}

		event.add(InterpolatedKVector.Builder.TYPE);
		event.add(DynamicKVector.Builder.TYPE);
		event.add(ScalarKVector.Builder.TYPE);
		event.add(OffsetKVector.Builder.TYPE);
		event.add(ScaledKVector.Builder.TYPE);
		event.add(FollowingEntityKVector.Builder.TYPE);
		event.add(FollowingPropKVector.Builder.TYPE);
		event.add(VariableKVector.Builder.TYPE);
		event.add(IfKVector.Builder.TYPE);
		event.add(PivotingKVector.Builder.TYPE);
		event.add(YRotatedKVector.Builder.TYPE);
		event.add(GroundKVector.Builder.TYPE);
	}

	@SubscribeEvent
	public static void entityFilterImBuilders(EntityFilterImBuilderEvent event) {
		for (var unit : EntityFilter.REGISTRY.unitValueMap().entrySet()) {
			event.addUnit(StringUtils.snakeCaseToTitleCase(unit.getKey()), unit.getValue());
		}

		event.add(EntityNotFilter.Builder.TYPE);
		event.add(EntityAndFilter.Builder.TYPE);
		event.add(EntityOrFilter.Builder.TYPE);
		event.add(EntityXorFilter.Builder.TYPE);

		event.add(ExactEntityFilter.IDBuilder.TYPE);
		event.add(ExactEntityFilter.UUIDBuilder.TYPE);
		event.add(EntityTagFilter.Builder.TYPE);
		event.add(EntityTypeFilter.Builder.TYPE);
		event.add(EntityTypeTagFilter.Builder.TYPE);
		event.add(MatchEntityFilter.Builder.TYPE);
		event.add(HasEffectEntityFilter.Builder.TYPE);
		event.add(ServerDataEntityFilter.Builder.TYPE);
		event.add(PlayerDataEntityFilter.Builder.TYPE);
		event.add(ProfileEntityFilter.Builder.TYPE);
		event.add(HasItemEntityFilter.Builder.TYPE);
		event.add(InDimensionEntityFilter.Builder.TYPE);
		event.add(IfEntityFilter.Builder.TYPE);
	}

	@SubscribeEvent
	public static void blockFilterImBuilders(BlockFilterImBuilderEvent event) {
		for (var unit : BlockFilter.REGISTRY.unitValueMap().entrySet()) {
			event.addUnit(StringUtils.snakeCaseToTitleCase(unit.getKey()), unit.getValue());
		}

		event.add(BlockNotFilter.Builder.TYPE);
		event.add(BlockAndFilter.Builder.TYPE);
		event.add(BlockOrFilter.Builder.TYPE);
		event.add(BlockXorFilter.Builder.TYPE);

		event.add(BlockIdFilter.Builder.TYPE);
		event.add(BlockStateFilter.Builder.TYPE);
		event.add(BlockTypeTagFilter.Builder.TYPE);
	}

	@SubscribeEvent
	public static void interpolationImBuilders(InterpolationImBuilderEvent event) {
		for (var type : InterpolationType.getMap().values()) {
			if (type.unit() != null) {
				event.addUnit(StringUtils.snakeCaseToTitleCase(type.name()), type.unit());
			}
		}
	}

	@SubscribeEvent
	public static void entityNumberImBuilders(EntityNumberImBuilderEvent event) {
		for (var unit : EntityNumber.REGISTRY.unitValueMap().entrySet()) {
			event.addUnit(StringUtils.snakeCaseToTitleCase(unit.getKey()), unit.getValue());
		}
	}
}
