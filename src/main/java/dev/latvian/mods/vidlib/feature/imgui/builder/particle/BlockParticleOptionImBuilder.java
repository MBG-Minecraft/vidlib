package dev.latvian.mods.vidlib.feature.imgui.builder.particle;

import dev.latvian.mods.vidlib.feature.block.ExactBlockStateImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;

public class BlockParticleOptionImBuilder implements ParticleOptionsImBuilder<BlockParticleOption> {
	public final ParticleType<BlockParticleOption> type;
	public final ExactBlockStateImBuilder block;

	public BlockParticleOptionImBuilder(ParticleType<BlockParticleOption> type) {
		this.type = type;
		this.block = new ExactBlockStateImBuilder(null);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return block.imgui(graphics);
	}

	@Override
	public BlockParticleOption build() {
		return new BlockParticleOption(type, block.build());
	}

	@Override
	public boolean isValid() {
		return block.isValid();
	}
}
