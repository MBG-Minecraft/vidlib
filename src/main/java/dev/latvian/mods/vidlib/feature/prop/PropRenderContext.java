package dev.latvian.mods.vidlib.feature.prop;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.vidlib.util.client.FrameInfo;

import java.util.Comparator;

public record PropRenderContext<P extends Prop>(
	P prop,
	PropRenderer<P> renderer,
	double x,
	double y,
	double z,
	double distanceToCamera,
	PoseStack poseStack,
	FrameInfo frame,
	float delta
) {
	public static final Comparator<PropRenderContext<?>> COMPARATOR = (a, b) -> Double.compare(b.distanceToCamera, a.distanceToCamera);

	public void render() {
		poseStack.pushPose();
		poseStack.translate(frame.x(x), frame.y(y), frame.z(z));
		renderer.render(this);
		poseStack.popPose();
	}
}
