#version 330
#extension GL_ARB_separate_shader_objects : require

//@formatter:off
#include <minecraft:dynamictransforms.glsl>
//@formatter:on

layout(std140) uniform ChromaKey {
	vec4 KeyColor;
	vec2 KeyCC;
	vec2 Range;
};

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

vec2 RGBToCC(vec4 rgba) {
	float Y = 0.299 * rgba.r + 0.587 * rgba.g + 0.114 * rgba.b;
	return vec2((rgba.b - Y) * 0.565, (rgba.r - Y) * 0.713);
}

void main() {
	vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;

	if (color.a == 0.0) {
		discard;
	} else {
		vec2 CC = RGBToCC(color);
		float mask = sqrt(pow(KeyCC.x - CC.x, 2.0) + pow(KeyCC.y - CC.y, 2.0));
		mask = smoothstep(Range.x, Range.y, mask);

		if (mask == 0.0) {
			discard;
		} else if (mask == 1.0) {
			fragColor = color;
		} else {
			fragColor = max(color - (1.0 - mask) * KeyColor, 0.0);
		}
	}
}
