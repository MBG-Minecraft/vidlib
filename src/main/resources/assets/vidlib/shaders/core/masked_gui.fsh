#version 330
#extension GL_ARB_separate_shader_objects : require

//@formatter:off
#include <minecraft:dynamictransforms.glsl>
//@formatter:on

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

// paired with minecraft:core/position_tex_color.vsh (texCoord0 = 0, vertexColor = 1)
layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
	vec4 color = texture(Sampler0, texCoord0) * vertexColor;
	color.a *= texture(Sampler1, texCoord0).a;

	if (color.a == 0.0) {
		discard;
	}
	fragColor = color * ColorModulator;
}
