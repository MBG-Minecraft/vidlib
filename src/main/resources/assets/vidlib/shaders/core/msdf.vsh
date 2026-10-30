#version 330
#extension GL_ARB_separate_shader_objects : require

#include < minecraft:projection.glsl >
#include < minecraft:dynamictransforms.glsl >

// DefaultVertexFormat.POSITION_TEX_COLOR = Position, UV0, Color
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

layout(location = 2) out vec4 vertexColor;
layout(location = 6) out vec2 texCoord0;

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

	vertexColor = Color;
	texCoord0 = UV0;
}
