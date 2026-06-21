#version 330

in vec2 texCoord0;
in vec4 vertexColor;

uniform sampler2D Texture;

out vec4 fragColor;

void main() {
	fragColor = vertexColor * texture(Texture, texCoord0.st);
}