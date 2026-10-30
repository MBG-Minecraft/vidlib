#version 330
#extension GL_ARB_separate_shader_objects : require

#include < minecraft:dynamictransforms.glsl >

uniform sampler2D Sampler0;

// paired with minecraft:core/position_tex_color.vsh (texCoord0 = 0, vertexColor = 1)
layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
	float d = texture(Sampler0, texCoord0).r;
	float d1 = d * d * d * d * d * d;
	vec4 color = vec4(d1, d1, d1, 1.0) * vertexColor;
	if (color.a == 0.0) {
		discard;
	}
	fragColor = color * ColorModulator;
}
