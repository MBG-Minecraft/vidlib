package dev.beast.mods.shimmer;

import dev.beast.mods.shimmer.content.clock.ClockContent;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Shimmer.ID, bus = EventBusSubscriber.Bus.MOD)
public class ShimmerDataGen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		var generator = event.getGenerator();
		var output = generator.getPackOutput();
		var existingFileHelper = event.getExistingFileHelper();
		var lookupProvider = event.getLookupProvider();

		generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
		generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ModBlockModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
	}

	private static class ModLanguageProvider extends LanguageProvider {
		public ModLanguageProvider(PackOutput output) {
			super(output, Shimmer.ID, "en_us");
		}

		@Override
		protected void addTranslations() {
			addBlock(ClockContent.BLOCK, "Clock");
		}
	}

	private static class ModItemModelProvider extends ItemModelProvider {
		public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
			super(output, Shimmer.ID, existingFileHelper);
		}

		@Override
		protected void registerModels() {
			basicItem(ClockContent.ITEM.get());
		}
	}

	private static class ModBlockModelProvider extends BlockModelProvider {
		public ModBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
			super(output, Shimmer.ID, existingFileHelper);
		}

		@Override
		protected void registerModels() {
		}
	}

	private static class ModBlockStateProvider extends BlockStateProvider {
		public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
			super(output, Shimmer.ID, exFileHelper);
		}

		@Override
		protected void registerStatesAndModels() {
			simpleBlock(ClockContent.BLOCK.get(), models().getBuilder("block/clock").texture("particle", "item/clock"));
		}
	}
}
