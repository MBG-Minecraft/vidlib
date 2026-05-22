package dev.latvian.mods.vidlib;

import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.feature.block.BlockStatePalette;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.clock.ClockFont;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.clothing.ClothingParts;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.entity.EntitySnapshot;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.environment.FogOverride;
import dev.latvian.mods.vidlib.feature.explosion.ExplosionData;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.location.Location;
import dev.latvian.mods.vidlib.feature.maptextureoverride.MapTextureOverrides;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.feature.skybox.SkyboxData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.stage.Stage;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.feature.zone.ZoneContainer;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.List;
import java.util.Set;

public interface VidLibDataTypes {
	DataType<Set<String>> STRING_SET = DataTypes.STRING.setOf();
	DataType<List<ItemStack>> ITEM_STACK_LIST = DataTypes.ITEM_STACK.listOf();

	// Necessary for Bukkit, used in Sleeping Player prop, etc.
	DataType<ItemStack> SAFE_ITEM_STACK = DataType.of(ItemStack.OPTIONAL_CODEC, new StreamCodec<>() {
		public static final ResourceLocation EMPTY_ID = ResourceLocation.tryBuild("minecraft", "air");

		@Override
		public ItemStack decode(RegistryFriendlyByteBuf buf) {
			var item = BuiltInRegistries.ITEM.get(buf.readResourceLocation()).orElse(null);
			return item == null || item.value() == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, 1, DataComponentPatch.EMPTY);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ItemStack stack) {
			if (stack.isEmpty()) {
				buf.writeResourceLocation(EMPTY_ID);
			} else {
				buf.writeResourceLocation(stack.getItemHolder().unwrapKey().map(ResourceKey::location).orElse(EMPTY_ID));
			}
		}
	}, ItemStack.class);

	DataType<ConnectionType> CONNECTION_TYPE = DataType.of(ConnectionType.values());

	static void register() {
		DataType.register(VidLib.id("string_set"), STRING_SET);
		DataType.register(VidLib.id("item_stack_list"), ITEM_STACK_LIST);
		DataType.register(VidLib.id("safe_item_stack"), SAFE_ITEM_STACK);
		DataType.register(VidLib.id("connection_type"), CONNECTION_TYPE);

		DataType.register(VidLib.id("icon"), Icon.DATA_TYPE);
		DataType.register(VidLib.id("clothing_parts"), ClothingParts.DATA_TYPE);
		DataType.register(VidLib.id("clothing"), Clothing.DATA_TYPE);
		DataType.register(VidLib.id("clothing_list"), Clothing.LEGACY_CLOTHING_DATA ? Clothing.LEGACY_LIST_DATA_TYPE : Clothing.LIST_DATA_TYPE);
		DataType.register(VidLib.id("skin_texture"), SkinTexture.DATA_TYPE);
		DataType.register(VidLib.id("skin_texture_list"), SkinTexture.LIST_DATA_TYPE);
		DataType.register(VidLib.id("skybox_id"), SkyboxData.ID_DATA_TYPE);
		DataType.register(VidLib.id("fog_override"), FogOverride.DATA_TYPE);
		DataType.register(VidLib.id("chanced_particle"), ChancedParticle.DATA_TYPE);
		DataType.register(VidLib.id("chanced_particle_list"), ChancedParticle.LIST_DATA_TYPE);
		DataType.register(VidLib.id("block_filter"), BlockFilter.DATA_TYPE);
		DataType.register(VidLib.id("entity_filter"), EntityFilter.DATA_TYPE);
		DataType.register(VidLib.id("zone_render_type"), ZoneRenderType.DATA_TYPE);
		DataType.register(VidLib.id("anchor"), Anchor.DATA_TYPE);
		DataType.register(VidLib.id("direct_cutscene"), Cutscene.DIRECT_DATA_TYPE);
		DataType.register(VidLib.id("cutscene"), Cutscene.DATA_TYPE, Cutscene.REGISTRY, null);
		DataType.register(VidLib.id("screen_shake"), ScreenShake.DATA_TYPE);
		DataType.register(VidLib.id("physics_particle_data"), PhysicsParticleData.DATA_TYPE);
		DataType.register(VidLib.id("positioned_block"), PositionedBlock.DATA_TYPE);
		DataType.register(VidLib.id("positioned_block_list"), PositionedBlock.LIST_DATA_TYPE);
		DataType.register(VidLib.id("prop_type"), PropType.DATA_TYPE);
		DataType.register(VidLib.id("explosion_data"), ExplosionData.DATA_TYPE);
		DataType.register(VidLib.id("clock_font_ref"), ClockFont.REF_DATA_TYPE);
		DataType.register(VidLib.id("zone_container"), ZoneContainer.DATA_TYPE, ZoneContainer.REGISTRY, null);
		DataType.register(VidLib.id("location"), Location.DATA_TYPE, Location.REGISTRY, null);
		DataType.register(VidLib.id("positioned_sound_data"), PositionedSoundData.DATA_TYPE);
		DataType.register(VidLib.id("knumber"), KNumber.DATA_TYPE);
		DataType.register(VidLib.id("kvector"), KVector.DATA_TYPE);
		DataType.register(VidLib.id("stage"), Stage.DATA_TYPE);
		DataType.register(VidLib.id("name_draw_type"), NameDrawType.DATA_TYPE);
		DataType.register(VidLib.id("block_state_palette"), BlockStatePalette.DATA_TYPE);
		DataType.register(VidLib.id("entity_snapshot"), EntitySnapshot.DATA_TYPE);
		DataType.register(VidLib.id("entity_snapshot_list"), EntitySnapshot.LIST_DATA_TYPE);
		DataType.register(VidLib.id("waypoint"), Waypoint.DATA_TYPE);
		DataType.register(VidLib.id("waypoint_list"), Waypoint.LIST_DATA_TYPE);
		DataType.register(VidLib.id("player_input"), PlayerInput.DATA_TYPE);
		DataType.register(VidLib.id("map_texture_overrides"), MapTextureOverrides.DATA_TYPE);
	}
}
