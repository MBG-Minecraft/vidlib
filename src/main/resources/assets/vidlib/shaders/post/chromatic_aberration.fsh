#version 410 core

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform Strength {
	float strength;
};

layout(std140) uniform Angle {
	float angle;
};

layout(std140) uniform FocusPos {
	vec2 focusPos;
};

layout (location = 0) in vec2 texCoord;
layout (location = 1) in vec2 oneTexel;

layout (location = 0) out vec4 fragColor;

void main() {
	vec2 toCenter;

	if (angle >= 0.0) {
		toCenter = vec2(cos(angle), sin(angle)) * strength;
	} else {
		toCenter = (focusPos - (texCoord * 2.0 - 1.0)) * abs(strength);
	}

	vec3 c;

	if (strength > 0.0) {
		fragColor = vec4(
				texture(InSampler, texCoord).r,
				texture(InSampler, texCoord + toCenter).g,
				texture(InSampler, texCoord + toCenter * 2.0).b,
				1.0);
	} else {
		fragColor = vec4(
				texture(InSampler, texCoord + toCenter * 2.0).r,
				texture(InSampler, texCoord + toCenter).g,
				texture(InSampler, texCoord).b,
				1.0);
	}
}
