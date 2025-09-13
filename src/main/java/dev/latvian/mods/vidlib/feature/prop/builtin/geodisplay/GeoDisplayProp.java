package dev.latvian.mods.vidlib.feature.prop.builtin.geodisplay;

import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.GeoAnimationsImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GeoModelImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GeoTextureImBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.geo.BaseGeoProp;
import net.minecraft.resources.ResourceLocation;

public class GeoDisplayProp extends BaseGeoProp {
	public static final PropData<GeoDisplayProp, ResourceLocation> MODEL = PropData.create(GeoDisplayProp.class, "model", ID.DATA_TYPE, p -> p.model, (p, v) -> p.model = v, GeoModelImBuilder.TYPE);
	public static final PropData<GeoDisplayProp, ResourceLocation> TEXTURE = PropData.create(GeoDisplayProp.class, "texture", ID.DATA_TYPE, p -> p.texture, (p, v) -> p.texture = v, GeoTextureImBuilder.TYPE);
	public static final PropData<GeoDisplayProp, ResourceLocation> ANIMATIONS = PropData.create(GeoDisplayProp.class, "animations", ID.DATA_TYPE, p -> p.animations, (p, v) -> p.animations = v, GeoAnimationsImBuilder.TYPE);

	@AutoRegister
	public static final PropType<GeoDisplayProp> TYPE = PropType.create(VidLib.id("geo_display"), GeoDisplayProp::new,
		TICK,
		POSITION,
		HEIGHT,
		YAW,
		MODEL,
		TEXTURE,
		ANIMATIONS
	);

	public ResourceLocation model = VidLib.id("prop/skeleton");
	public ResourceLocation texture = ID.mc("textures/entity/skeleton/skeleton.png");
	public ResourceLocation animations = Empty.ID;

	public GeoDisplayProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 0F;
		this.height = 1F;
		this.gravity = 0F;
	}

	@Override
	public double getMaxRenderDistance() {
		return 1024D;
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		return true;
	}
}
