#version 410 core

uniform sampler2D InSampler;

in vec2 texCoord;

layout (location = 0) out vec4 fragColor;

void main() {
	vec4 c = texture(InSampler, texCoord);

	if (c.a == 0.0) {
		discard;
	}

	fragColor = c;
}
