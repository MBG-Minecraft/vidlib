#version 330

#moj_import <minecraft:projection.glsl>

in vec2 Position;
in vec2 UV;
in vec4 Color;

out vec2 texCoord0;
out vec4 vertexColor;

void main() {
	texCoord0 = UV;
	vertexColor = Color;
	gl_Position = ProjMat * vec4(Position.xy, 0.0, 1.0);
}