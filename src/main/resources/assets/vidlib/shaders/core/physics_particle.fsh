#version 330
#extension GL_ARB_separate_shader_objects : require

#include < minecraft:fog.glsl >
#include < minecraft:dynamictransforms.glsl >

uniform sampler2D Sampler0;

layout(location = 6) in vec2 texCoord0;
layout(location = 2) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
	vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
	#ifdef ALPHA_CUTOUT
	if (color.a < ALPHA_CUTOUT) {
		discard;
	}
	#endif
	fragColor = color;
}
