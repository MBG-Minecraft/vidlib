#version 330
#extension GL_ARB_separate_shader_objects : require

#include < minecraft:dynamictransforms.glsl >
#include < minecraft:projection.glsl >

// DefaultVertexFormat.BLOCK = Position, Color, UV0, UV2 (no Normal attribute)
layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;

layout(location = 2) out vec4 vertexColor;
layout(location = 6) out vec2 texCoord0;

void main() {
	vec3 pos = Position + ModelOffset;
	gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

	vertexColor = Color;
	texCoord0 = UV0;
}
