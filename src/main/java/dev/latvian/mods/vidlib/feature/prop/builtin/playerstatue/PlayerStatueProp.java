package dev.latvian.mods.vidlib.feature.prop.builtin.playerstatue;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.klib.math.FrustumCheck;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoRegister;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
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

public class PlayerStatueProp extends BaseGeoProp {
	public static final PropData<PlayerStatueProp, Component> NAME = PropData.create(PlayerStatueProp.class, "name", DataTypes.TEXT_COMPONENT, p -> p.name, (p, v) -> p.name = v, TextComponentImBuilder.TYPE);
	public static final PropData<PlayerStatueProp, Boolean> STONE = PropData.createBoolean(PlayerStatueProp.class, "stone", p -> p.stone, (p, v) -> p.stone = v);
	public static final PropData<PlayerStatueProp, GameProfile> PROFILE = PropData.create(PlayerStatueProp.class, "profile", DataTypes.GAME_PROFILE, p -> p.profile, (p, v) -> p.profile = v, GameProfileImBuilder.TYPE);

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
		PROFILE
	);

	public Component name;
	public boolean stone;
	public GameProfile profile;

	public PlayerStatueProp(PropContext<?> ctx) {
		super(ctx);
		this.width = 0.6F;
		this.height = 1.8F;
		this.name = Empty.COMPONENT;
		this.gravity = 0F;
		this.stone = false;
		this.profile = Empty.PROFILE;
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
