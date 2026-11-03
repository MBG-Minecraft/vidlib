package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.render.SubmitContext;
import dev.latvian.mods.klib.render.Submittable;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.vidlib.feature.client.VidLibExtractContext;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.renderstate.BaseRenderState;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.Map;

public class PropRenderState<P extends Prop> extends BaseRenderState implements Submittable, Comparable<PropRenderState<?>> {
	public static final Comparator<PropRenderState<?>> COMPARATOR = PropRenderState::compareTo;
	public static final Lazy<Map<PropType<?>, PropRenderStateFactory<?, ?>>> BY_TYPE = Lazy.identityMap(map -> NeoForge.EVENT_BUS.post(new PropRenderStateRegistryEvent(map::put)));

	public boolean invisible = false;
	public double x = 0D;
	public double y = 0D;
	public double centerY = 0D;
	public double z = 0D;
	public double squaredDistanceToCamera = 0D;
	public boolean shouldSort = false;

	public final void extractCore(P prop, VidLibExtractContext ctx) {
		invisible = false;
		double delta = ctx.delta();
		x = Mth.lerp(delta, prop.prevPos.x, prop.pos.x);
		y = Mth.lerp(delta, prop.prevPos.y, prop.pos.y);
		z = Mth.lerp(delta, prop.prevPos.z, prop.pos.z);
		centerY = y + prop.getCenterYOffset();
		squaredDistanceToCamera = ctx.cameraPos().distanceToSqr(x, centerY, z);
	}

	public void extract(P prop, VidLibExtractContext ctx) {
	}

	@Override
	public void submit(SubmitContext ctx) {
	}

	@Override
	public int compareTo(@NonNull PropRenderState<?> o) {
		return Double.compare(o.squaredDistanceToCamera, squaredDistanceToCamera);
	}
}
