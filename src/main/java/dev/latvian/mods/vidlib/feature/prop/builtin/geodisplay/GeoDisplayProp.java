package dev.latvian.mods.vidlib.feature.prop.builtin.geodisplay;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.GeoAnimationsImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GeoModelImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.geo.BaseGeoProp;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class GeoDisplayProp extends BaseGeoProp {
	@AutoRegister
	public static final PropType<GeoDisplayProp> TYPE = PropType.create(ID.vidlib("geo_display"), GeoDisplayProp::new,
		TICK,
		POSITION,
		HEIGHT,
		YAW,
		PropData.create(GeoDisplayProp.class, "model", ID.DATA_TYPE, p -> p.model, (p, v) -> p.model = v, GeoModelImBuilder.TYPE),
		PropData.create(GeoDisplayProp.class, "texture", DataTypes.RESOURCE_TEXTURE, p -> p.texture, (p, v) -> p.texture = v, TextureImBuilder.GEO),
		PropData.create(GeoDisplayProp.class, "animations", ID.DATA_TYPE, p -> p.animations, (p, v) -> p.animations = v, GeoAnimationsImBuilder.TYPE)
	);

	public Identifier model = ID.vidlib("prop/player");
	public ClientAsset.ResourceTexture texture = SkinTexture.WIDE_STEVE.asset();
	public Identifier animations = Empty.ID;

	public GeoDisplayProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 0F;
		this.height = 1F;
	}

	@Override
	public double getMaxRenderDistance() {
		return 1024D;
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum, Vec3 camera, double squaredCenterDistanceToCamera) {
		return true;
	}
}
