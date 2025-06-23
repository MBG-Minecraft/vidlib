package dev.latvian.mods.vidlib.feature.highlight;

import dev.latvian.mods.klib.color.Gradient;
import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.shape.Shape;
import dev.latvian.mods.klib.vertex.VertexCallback;
import dev.latvian.mods.vidlib.math.worldnumber.WorldNumberVariables;
import dev.latvian.mods.vidlib.math.worldvector.FixedWorldVector;
import dev.latvian.mods.vidlib.math.worldvector.WorldVector;
import dev.latvian.mods.vidlib.util.client.FrameInfo;

public class TerrainHighlightInstance {
	public final WorldVector position;
	public final Shape shape;
	public final Gradient color;
	public final WorldVector scale;
	public final int duration;
	public int prevTick;
	public int tick;

	public TerrainHighlightInstance(TerrainHighlight highlight) {
		this.position = highlight.position();
		this.shape = highlight.shape();
		this.color = highlight.color().optimize();
		this.scale = highlight.scale();
		this.duration = highlight.duration();
		this.tick = 0;
	}

	public boolean tick() {
		prevTick = tick;
		return ++tick >= duration;
	}

	public void render(FrameInfo frame, VertexCallback buffer, WorldNumberVariables variables) {
		var ms = frame.poseStack();
		var t = KMath.lerp(frame.worldDelta(), prevTick, tick) / (float) duration;
		var ctx = frame.mc().level.globalContext(t).withVariables(variables);
		var pos = position.get(ctx);

		if (pos != null) {
			ms.pushPose();
			frame.translate(pos);

			var s = scale.get(ctx);

			if (s != null && s != FixedWorldVector.ONE.instance().vec()) {
				ms.scale((float) s.x, (float) s.y, (float) s.z);
			}

			shape.buildQuads(0F, 0F, 0F, ms.last().transform(buffer).withColor(color.get(t)));
			ms.popPose();
		}
	}
}
