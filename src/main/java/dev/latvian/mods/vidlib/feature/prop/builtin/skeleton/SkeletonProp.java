package dev.latvian.mods.vidlib.feature.prop.builtin.skeleton;

import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.geo.BaseGeoProp;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class SkeletonProp extends BaseGeoProp {
	public static final PropData<SkeletonProp, Component> NAME = PropData.create(SkeletonProp.class, "name", DataTypes.TEXT_COMPONENT, p -> p.name, (p, v) -> p.name = v, TextComponentImBuilder.TYPE);
	public static final PropData<SkeletonProp, Easing> FALL_EASING = PropData.create(SkeletonProp.class, "fall_easing", Easing.DATA_TYPE, p -> p.fallEasing, (p, v) -> p.fallEasing = v, EnumImBuilder.EASING_TYPE);

	@AutoRegister
	public static final PropType<SkeletonProp> TYPE = PropType.create(VidLib.id("skeleton"), SkeletonProp::new,
		TICK,
		POSITION,
		HEIGHT,
		YAW,
		NAME,
		FALL_EASING
	);

	public Component name;
	public Easing fallEasing;

	public SkeletonProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 0.6F;
		this.height = 2F;
		this.name = Empty.COMPONENT;
		this.fallEasing = Easing.EXPO_IN; // BOUNCE_OUT
		this.gravity = 0F;
	}

	@Override
	public float getPitch(float delta) {
		float tick = getTick(delta);

		if (tick >= 80F) {
			return fallEasing.lerp(1F, 0F, -90F);
		} else if (tick >= 40F) {
			return fallEasing.lerp((tick - 40F) / 40F, 0F, -90F);
		} else {
			return fallEasing.lerp(0F, 0F, -90F);
		}
	}

	@Override
	public double getMaxRenderDistance() {
		return 256D * height;
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		double s = height * 1.05D;
		return frustum.isVisible(x - s, y - s * 0.2D, z - s, x + s, y + s, z + s);
	}

	@Override
	@Nullable
	public Vec3 getInfoPos(float delta) {
		var pos = getPos(delta);
		var mat = new Matrix3f();
		mat.rotateY((float) Math.toRadians(getYaw(delta)));
		mat.rotateX((float) Math.toRadians(getPitch(delta)));
		var vec = mat.transform(new Vector3f(0F, (float) (height * 0.9D), 0F));
		return pos.add(vec.x, vec.y + height * 0.2D, vec.z);
	}

	@Override
	public Component getDisplayName() {
		return Empty.isEmpty(name) ? Component.literal(type.translationKey()) : name;
	}

	@Override
	public boolean shouldRenderDisplayName(Player to) {
		return !Empty.isEmpty(name);
	}
}
