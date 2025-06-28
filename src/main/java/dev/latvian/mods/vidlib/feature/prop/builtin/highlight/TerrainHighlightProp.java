package dev.latvian.mods.vidlib.feature.prop.builtin.highlight;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.Vec3f;
import dev.latvian.mods.klib.shape.CircleShape;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.prop.Prop;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropDataProvider;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.math.kvector.KVector;

public class TerrainHighlightProp extends Prop {
	public static final PropData<TerrainHighlightProp, Shape> SHAPE = PropData.create(TerrainHighlightProp.class, "shape", Shape.DATA_TYPE, p -> p.shape, (p, v) -> p.shape = v);
	public static final PropData<TerrainHighlightProp, Gradient> COLOR = PropData.create(TerrainHighlightProp.class, "color", Gradient.DATA_TYPE, p -> p.color, (p, v) -> p.color = v.optimize());
	public static final PropData<TerrainHighlightProp, KVector> SCALE = PropData.create(TerrainHighlightProp.class, "scale", KVector.DATA_TYPE, p -> p.scale, (p, v) -> p.scale = v);

	@AutoRegister
	public static final PropType<TerrainHighlightProp> TYPE = PropType.create(VidLib.id("terrain_highlight"), TerrainHighlightProp::new, PropDataProvider.join(
		TICK,
		LIFESPAN,
		DYNAMIC_POSITION,
		SHAPE,
		COLOR,
		SCALE
	));

	public Shape shape;
	public Gradient color;
	public KVector scale;
	public Vec3f prevRenderScale;
	public Vec3f renderScale;

	public TerrainHighlightProp(PropContext<?> ctx) {
		super(ctx);
		this.lifespan = 20;
		this.gravity = 0F;
		this.shape = CircleShape.UNIT;
		this.color = Color.WHITE;
		this.scale = KVector.ONE;
		this.prevRenderScale = Vec3f.ONE;
		this.renderScale = Vec3f.ONE;
	}

	@Override
	public void snap() {
		super.snap();
		prevRenderScale = renderScale;
	}

	@Override
	public void onAdded() {
		super.onAdded();
		var s = scale.get(createWorldNumberContext());
		renderScale = s == null ? Vec3f.ONE : Vec3f.of(s);
		width = Math.max(renderScale.x(), renderScale.z());
	}

	@Override
	public void tick() {
		super.tick();
		var s = scale.get(createWorldNumberContext());
		renderScale = s == null ? Vec3f.ONE : Vec3f.of(s);
		width = Math.max(renderScale.x(), renderScale.z());
	}
}
