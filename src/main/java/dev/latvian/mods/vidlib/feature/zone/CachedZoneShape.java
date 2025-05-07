package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.kmath.VoxelShapeBox;
import dev.latvian.mods.vidlib.util.ResolvedCubeTextures;
import dev.latvian.mods.vidlib.util.ResolvedTexturedCube;
import dev.latvian.mods.vidlib.util.TerrainRenderLayer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CachedZoneShape {
	public static final CachedZoneShape[] EMPTY_ARRAY = new CachedZoneShape[0];

	private final ZoneInstance instance;
	private final VoxelShape shape;
	private VoxelShapeBox shapeBox;
	private Map<TerrainRenderLayer, List<ResolvedTexturedCube>> cachedCubes;

	public CachedZoneShape(ZoneInstance instance, VoxelShape shape) {
		this.instance = instance;
		this.shape = shape;
	}

	public static void append(Collection<CachedZoneShape> list, ZoneInstance instance) {
		var shape = instance.zone.shape().createVoxelShape().optimize();

		if (!shape.isEmpty()) {
			list.add(new CachedZoneShape(instance, shape));
		}
	}

	public ZoneInstance instance() {
		return instance;
	}

	public VoxelShape shape() {
		return shape;
	}

	public VoxelShapeBox shapeBox() {
		if (shapeBox == null) {
			shapeBox = VoxelShapeBox.of(shape);
		}

		return shapeBox;
	}

	@Override
	public String toString() {
		return "CachedZoneShape[" +
			"instance=" + instance + ", " +
			"shape=" + shape + ", " +
			"shapeBox=" + shapeBox + ']';
	}

	public Map<TerrainRenderLayer, List<ResolvedTexturedCube>> cachedCubes() {
		if (cachedCubes == null) {
			cachedCubes = new Reference2ObjectOpenHashMap<>();

			double yOff = 0D;

			if (!instance.zone.fluid().isEmpty()) {
				yOff = 1D - instance.zone.fluid().fluidState().getOwnHeight();
				// yOff += Math.clamp(-frame.y(yOff) / 50D, 0D, 0.5D);
			}

			for (var box : shapeBox().boxes()) {
				var bminX = box.minX;
				var bminY = box.minY;
				var bminZ = box.minZ;
				var bmaxX = box.maxX;
				var bmaxY = box.maxY - yOff;
				var bmaxZ = box.maxZ;

				var textures = ResolvedCubeTextures.EMPTY;

				if (!instance.zone.fluid().isEmpty()) {
					textures = textures.merge(ResolvedCubeTextures.resolve(instance.zone.fluid().textures()));
				}

				if (instance.zone.textures().isPresent()) {
					textures = textures.merge(ResolvedCubeTextures.resolve(instance.zone.textures().get()));
				}

				if (textures != ResolvedCubeTextures.EMPTY) {
					for (var renderLayerFilter : TerrainRenderLayer.ALL) {
						var tex = textures.filter(renderLayerFilter);

						if (tex != ResolvedCubeTextures.EMPTY) {
							cachedCubes.computeIfAbsent(renderLayerFilter, k -> new ArrayList<>(1)).add(new ResolvedTexturedCube(new AABB(bminX, bminY, bminZ, bmaxX, bmaxY, bmaxZ), tex));
						}
					}
				}
			}
		}

		return cachedCubes;
	}
}
