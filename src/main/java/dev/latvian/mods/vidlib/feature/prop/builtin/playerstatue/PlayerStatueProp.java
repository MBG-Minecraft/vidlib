package dev.latvian.mods.vidlib.feature.prop.builtin.playerstatue;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextComponentImBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropContext;
import dev.latvian.mods.vidlib.feature.prop.PropData;
import dev.latvian.mods.vidlib.feature.prop.PropType;
import dev.latvian.mods.vidlib.feature.prop.geo.BaseGeoProp;
import dev.latvian.mods.vidlib.util.SpreadType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class PlayerStatueProp extends BaseGeoProp {
	public static final PropData<PlayerStatueProp, Component> NAME = PropData.create(PlayerStatueProp.class, "name", DataTypes.TEXT_COMPONENT, p -> p.name, (p, v) -> p.name = v, TextComponentImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, Boolean> STONE = PropData.createBoolean(PlayerStatueProp.class, "stone", p -> p.stone, (p, v) -> p.stone = v);
	public static final PropData<PlayerStatueProp, GameProfile> PROFILE = PropData.create(PlayerStatueProp.class, "profile", DataTypes.GAME_PROFILE, p -> p.profile, (p, v) -> p.profile = v, GameProfileImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, Integer> COUNT = PropData.createInt(PlayerStatueProp.class, "count", p -> p.count, (p, v) -> p.count = v, 1, 1000);
	public static final PropData<PlayerStatueProp, SpreadType> SPREAD = PropData.create(PlayerStatueProp.class, "spread", SpreadType.DATA_TYPE, p -> p.spread, (p, v) -> p.spread = v, () -> new EnumImBuilder<>(SpreadType.VALUES));
	public static final PropData<PlayerStatueProp, Float> SPREAD_RADIUS = PropData.createFloat(PlayerStatueProp.class, "spread_radius", p -> p.spreadRadius, (p, v) -> p.spreadRadius = v, 0F, 200F);
	public static final PropData<PlayerStatueProp, Float> SPREAD_RANDOM = PropData.createFloat(PlayerStatueProp.class, "spread_random", p -> p.spreadRandom, (p, v) -> p.spreadRandom = v, 0F, 0.5F);

	@AutoRegister
	public static final PropType<PlayerStatueProp> TYPE = PropType.create(VidLib.id("player_statue"), PlayerStatueProp::new,
		TICK,
		POSITION,
		HEIGHT,
		YAW,
		PITCH,
		ROLL,
		NAME,
		STONE,
		PROFILE,
		COUNT,
		SPREAD,
		SPREAD_RADIUS,
		SPREAD_RANDOM
	);

	public Component name;
	public boolean stone;
	public GameProfile profile;
	public int count;
	public SpreadType spread;
	public float spreadRadius;
	public float spreadRandom;

	public PlayerStatueProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 0.6F;
		this.height = 1.8F;
		this.name = Empty.COMPONENT;
		this.gravity = 0F;
		this.stone = false;
		this.profile = Empty.PROFILE;
		this.count = 1;
		this.spread = SpreadType.LINE;
		this.spreadRadius = 10F;
		this.spreadRandom = 0.35F;
	}

	@Override
	public double getMaxRenderDistance() {
		return Math.max(spreadRadius, 256D * height);
	}

	@Override
	public boolean isVisible(double x, double y, double z, FrustumCheck frustum) {
		if (count > 1) {
			return true;
		}

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
