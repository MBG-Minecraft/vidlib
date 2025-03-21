package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerStructureTemplate;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin implements ShimmerStructureTemplate {
	@Shadow
	@Final
	public List<StructureTemplate.Palette> palettes;

	@Shadow
	@Final
	private List<StructureTemplate.StructureEntityInfo> entityInfoList;

	@Shadow
	private Vec3i size;

	@Shadow
	private static void addToLists(StructureTemplate.StructureBlockInfo blockInfo, List<StructureTemplate.StructureBlockInfo> normalBlocks, List<StructureTemplate.StructureBlockInfo> blocksWithNbt, List<StructureTemplate.StructureBlockInfo> blocksWithSpecialShape) {
	}

	/*
	public void fillBlocksFromWorld(Level level, Stream<BlockPos> blocks, BlockFilter filter) {


		if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
			BlockPos blockpos = pos.offset(size).offset(-1, -1, -1);
			List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
			List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();
			List<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();
			BlockPos blockpos1 = new BlockPos(
				Math.min(pos.getX(), blockpos.getX()), Math.min(pos.getY(), blockpos.getY()), Math.min(pos.getZ(), blockpos.getZ())
			);
			BlockPos blockpos2 = new BlockPos(
				Math.max(pos.getX(), blockpos.getX()), Math.max(pos.getY(), blockpos.getY()), Math.max(pos.getZ(), blockpos.getZ())
			);
			this.size = size;

			for (BlockPos blockpos3 : BlockPos.betweenClosed(blockpos1, blockpos2)) {
				BlockPos blockpos4 = blockpos3.subtract(blockpos1);
				BlockState blockstate = level.getBlockState(blockpos3);
				if (toIgnore == null || !blockstate.is(toIgnore)) {
					BlockEntity blockentity = level.getBlockEntity(blockpos3);
					StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo;
					if (blockentity != null) {
						structuretemplate$structureblockinfo = new StructureTemplate.StructureBlockInfo(
							blockpos4, blockstate, blockentity.saveWithId(level.registryAccess())
						);
					} else {
						structuretemplate$structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos4, blockstate, null);
					}

					addToLists(structuretemplate$structureblockinfo, list, list1, list2);
				}
			}

			List<StructureTemplate.StructureBlockInfo> list3 = buildInfoList(list, list1, list2);
			this.palettes.clear();
			this.palettes.add(new StructureTemplate.Palette(list3));
			this.entityInfoList.clear();
		}
	}
	 */
}
