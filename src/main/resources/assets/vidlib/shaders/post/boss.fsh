#version 410 core

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D MainDepthSampler;

in vec2 texCoord;

layout (location = 0) out vec4 fragColor;

void main() {
	vec4 c = texture(InSampler, texCoord);

	if (c.a == 0.0) {
		discard;
	}

	float id = texture(InDepthSampler, texCoord).r;
	float md = texture(MainDepthSampler, texCoord).r;

	if (id > md) {
		discard;
	}

	fragColor = c;
}
